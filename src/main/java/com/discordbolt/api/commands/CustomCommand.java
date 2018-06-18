package com.discordbolt.api.commands;

import com.discordbolt.api.commands.exceptions.CommandException;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.discordbolt.api.commands.ValidityCheck.*;

class CustomCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomCommand.class);

    private CommandManager manager;

    private List<String> command;

    private Method method;
    private Command commandClass;

    private String description;
    private String usage;
    private String module;
    private Set<String> aliases = new HashSet<>();
    private Set<String> channelNameWhitelist = new HashSet<>();
    private Set<String> channelNameBlacklist = new HashSet<>();
    private Set<Long> channelWhitelist = new HashSet<>();
    private Set<Long> channelBlacklist = new HashSet<>();
    private EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
    private int[] argRange;
    private boolean secret;
    private boolean allowDM;
    private boolean delete;

    CustomCommand(CommandManager manager, Method method) {
        this.manager = manager;

        BotCommand annotation = method.getAnnotation(BotCommand.class);
        this.command = Arrays.stream(annotation.command())
                             .map(String::toLowerCase)
                             .collect(Collectors.toList());
        this.method = method;
        this.module = annotation.module();
        this.description = annotation.description();
        this.usage = annotation.usage();
        this.aliases.addAll(Arrays.asList(annotation.aliases()));
        this.channelWhitelist.addAll(Arrays.stream(annotation.channelWhitelist())
                                           .boxed()
                                           .collect(Collectors.toList()));
        this.channelNameWhitelist.addAll(Arrays.asList(annotation.channelNameWhitelist()));
        this.channelBlacklist.addAll(Arrays.stream(annotation.channelBlacklist())
                                           .boxed()
                                           .collect(Collectors.toList()));
        this.channelNameBlacklist.addAll(Arrays.asList(annotation.channelNameBlacklist()));
        this.permissions.addAll(Arrays.asList(annotation.permissions()));
        this.argRange = annotation.args();
        this.secret = annotation.secret();
        this.allowDM = annotation.allowDM();
        this.delete = annotation.deleteCommandMessage();

        if (argRange.length >= 1 && getCommands().size() > argRange[0]) {
            throw new IllegalStateException("Too many subcommands for given arg count. Command: " + String
                    .join(" ", getCommands()));
        }

        if (argRange.length >= 2 && argRange[0] > argRange[1]) {
            throw new IllegalStateException("Argument range is invalid! Command: " + String
                    .join(" ", getCommands()));
        }

        if (allowDM && !permissions.isEmpty()) {
            throw new IllegalStateException("Can not execute command in DMs that require permissions. Command: " + String
                    .join(" ", getCommands()));
        }
    }

    CustomCommand(CommandManager manager, Command command) {
        this.manager = manager;

        this.commandClass = command;

        this.command = Arrays.stream(commandClass.getCommand())
                             .map(String::toLowerCase)
                             .collect(Collectors.toList());
        this.module = commandClass.getModule();
        this.description = commandClass.getDescription();
        this.usage = commandClass.getUsage();
        this.aliases.addAll(Arrays.asList(commandClass.getAliases()));
        this.channelWhitelist.addAll(Arrays.stream(commandClass.getChannelWhitelist())
                                           .boxed()
                                           .collect(Collectors.toList()));
        this.channelNameWhitelist.addAll(Arrays.asList(commandClass.getChannelNameWhitelist()));
        this.channelBlacklist.addAll(Arrays.stream(commandClass.getChannelBlacklist())
                                           .boxed()
                                           .collect(Collectors.toList()));
        this.channelNameBlacklist.addAll(Arrays.asList(commandClass.getChannelNameBlacklist()));
        this.permissions.addAll(Arrays.asList(commandClass.getPermissions()));
        this.argRange = commandClass.getArgs();
        this.secret = commandClass.isSecret();
        this.allowDM = commandClass.isAllowDM();
        this.delete = commandClass.isDeleteCommandMessage();

        if (argRange.length >= 1 && getCommands().size() > argRange[0]) {
            throw new IllegalStateException("Too many subcommands for given arg count. Command: " + String
                    .join(" ", getCommands()));
        }

        if (argRange.length >= 2 && argRange[0] > argRange[1]) {
            throw new IllegalStateException("Argument range is invalid! Command: " + String
                    .join(" ", getCommands()));
        }

        if (allowDM && !permissions.isEmpty()) {
            throw new IllegalStateException("Can not execute command in DMs that require permissions. Command: " + String
                    .join(" ", getCommands()));
        }
    }

    CommandManager getCommandManager() {
        return manager;
    }

    public List<String> getCommands() {
        return Collections.unmodifiableList(command);
    }

    public String getCommand(int index) {
        return getCommands().get(index);
    }

    public String getBaseCommand() {
        return getCommand(0);
    }

    public String getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage(Guild guild) {
        return manager.getCommandPrefix(guild) + String.join(" ", getCommands()) + usage;
    }

    public Optional<Integer> getMinimumArgCount() {
        if (argRange.length >= 1) {
            return Optional.of(argRange[0]);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Integer> getMaximumArgCount() {
        if (argRange.length == 1) {
            return Optional.of(argRange[0]);
        } else if (argRange.length >= 2) {
            return Optional.of(argRange[1]);
        } else {
            return Optional.empty();
        }
    }

    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public Set<Long> getChannelWhitelist() {
        return Collections.unmodifiableSet(channelWhitelist);
    }

    public Set<Long> getChannelBlacklist() {
        return Collections.unmodifiableSet(channelBlacklist);
    }

    public Set<String> getChannelNameWhitelist() {
        return Collections.unmodifiableSet(channelNameWhitelist);
    }

    public Set<String> getChannelNameBlacklist() {
        return Collections.unmodifiableSet(channelNameBlacklist);
    }

    public EnumSet<Permission> getPermissions() {
        return EnumSet.copyOf(permissions);
    }

    public boolean isSecret() {
        return secret;
    }

    public boolean allowDM() {
        return allowDM;
    }

    public boolean shouldDeleteMessages() {
        return delete;
    }

    private Flux<ValidityCheck.CheckResult> allPreChecks(CommandContext commandContext) {
        return Flux.concat(isAllowedChannel(commandContext), isValidArgumentCount(commandContext), hasPermission(commandContext));
    }

    private Flux<ValidityCheck.CheckResult> isAllowedChannel(CommandContext commandContext) {
        return Flux.concat(channelDM(this, commandContext), channelBlacklist(this, commandContext), channelNameBlacklist(this, commandContext), channelWhitelist(this, commandContext), channelNameWhitelist(this, commandContext));
    }

    private Flux<ValidityCheck.CheckResult> isValidArgumentCount(CommandContext commandContext) {
        return Flux.concat(argumentLowerBound(this, commandContext), argumentUpperBound(this, commandContext));
    }

    private Flux<ValidityCheck.CheckResult> hasPermission(CommandContext commandContext) {
        return Flux.concat(permission(this, commandContext));
    }

    protected void preexec(Message message) {
        CommandContext cc = new CommandContext(message, this);

        allPreChecks(cc).filter(checkResult -> checkResult != CheckResult.VALID)
                        .next()
                        .map(CheckResult::getMessage)
                        .flatMap(cc::replyWith)
                        .switchIfEmpty(this.executeCommand(cc))
                        .doFinally(signal -> {
                            LOGGER.info("Executing final on command " + cc.getUserBaseCommand());
                            if (shouldDeleteMessages()) {
                                message.delete().subscribe();
                            }
                            if (manager.getCommandConsumer() != null) {
                                manager.getCommandConsumer().accept(cc);
                            }
                        })
                        .subscribe();
    }

    private Mono<Message> executeCommand(CommandContext commandContext) {
        try {
            if (method != null)
                method.invoke(null, commandContext);
            else
                commandClass.execute(commandContext);
            return Mono.empty();
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof CommandException) {
                return commandContext.replyWith(ite.getCause().getMessage());
            } else {
                LOGGER.error("Uncaught exception during execution of command \"" + String.join(" ", getCommands()) + "\".");
                LOGGER.error(ite.getCause().getMessage());
                LOGGER.debug(ite.getCause().getMessage(), ite.getCause());
                return commandContext.replyWith(ExceptionMessages.COMMAND_PROCESS_EXCEPTION);
            }
        } catch (IllegalAccessException ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.debug(ex.getMessage(), ex);
            return commandContext.replyWith(ExceptionMessages.COMMAND_PROCESS_EXCEPTION);
        }
    }
}
