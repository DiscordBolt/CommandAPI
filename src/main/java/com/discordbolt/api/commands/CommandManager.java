package com.discordbolt.api.commands;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    private static String DEFAULT_PREFIX = "!";

    private DiscordClient client;
    private List<CustomCommand> commands = new ArrayList<>();
    private Map<Long, String> commandPrefixes = new HashMap<>();
    private Consumer<CommandContext> consumer;

    /**
     * Initialize Command API
     *
     * @param client        DiscordClient
     * @param packagePrefix package string where commands are located
     */
    public CommandManager(DiscordClient client, String packagePrefix) {
        LOGGER.info("Initializing Commands"); //TODO update to print version number (in application.properties)

        // Save DiscordClient
        this.client = client;

        // Get all public static methods with @BotCommand and create CustomCommand objects
        commands.addAll(new Reflections(packagePrefix, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(BotCommand.class).stream().filter(a -> Modifier.isStatic(a.getModifiers())).filter(a -> Modifier.isPublic(a.getModifiers())).map(a -> {
            try {
                return new CustomCommand(this, a);
            } catch (IllegalStateException e) {
                LOGGER.error("Command Initialization exception.", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));

        // Register our help command
        registerCommand(new HelpCommand(this));

        // Sort all registered commands for the Help Module
        sortCommands();

        // Register our command listener
        CommandListener commandListener = new CommandListener(this, client);
    }

    public void registerCommand(Command command) {
        commands.add(new CustomCommand(this, command));
        sortCommands();
    }

    /**
     * Set the command prefix of a specified guild
     *
     * @param guild         Guild to change the prefix for
     * @param commandPrefix new prefix string all commands must be prefaced with
     */
    public void setCommandPrefix(Guild guild, String commandPrefix) {
        setCommandPrefix(guild.getId().asLong(), commandPrefix);
    }

    public void setCommandPrefix(long guildID, String commandPrefix) {
        commandPrefixes.put(guildID, commandPrefix);
    }

    public void disableHelpCommand() {
        commands.removeIf(command -> command.getCommands().equals(Collections.singletonList("help")));
    }

    public void onCommandExecution(Consumer<CommandContext> consumer) {
        this.consumer = consumer;
    }

    protected Consumer<CommandContext> getCommandConsumer() {
        return consumer != null ? consumer : (c) -> {LOGGER.error("NULL CONSUMER");};
    }

    /**
     * Get the Discord4J client
     *
     * @return IDiscordClient
     */
    protected DiscordClient getClient() {
        return client;
    }

    /**
     * Get a list of all commands currently registered
     *
     * @return UnmodifiableList of CustomCommands
     */
    protected List<CustomCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Get the command prefix of a given guild
     *
     * @param guild
     * @return char command prefix for given guild
     */
    protected String getCommandPrefix(Guild guild) {
        if (guild == null)
            return DEFAULT_PREFIX;
        return getCommandPrefix(guild.getId().asLong());
    }

    protected String getCommandPrefix(long guildID) {
        return commandPrefixes.getOrDefault(guildID, DEFAULT_PREFIX);
    }

    private void sortCommands() {
        commands.sort(Comparator.comparing(c -> (c.getModule() + " " + String.join(" ", c.getCommands()))));
    }
}
