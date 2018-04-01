package com.discordbolt.api.command;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Tony on 2/17/2017.
 */
public class CommandManager {

    private String globalDefaultPrefix;
    private IDiscordClient client;
    private Logger logger = LoggerFactory.getLogger(CommandManager.class);
    private List<CustomCommand> commands = new ArrayList<>();
    private Map<Long, String> commandPrefixes = new HashMap<>();

    /**
     * Initalize Command API
     *
     * @param client        IDiscordClient
     * @param packagePrefix package string where commands are located
     */
    public CommandManager(IDiscordClient client, String packagePrefix, String defaultPrefix) {
        logger.info("Initializing Commands");

        // Check prefix for space
        if(defaultPrefix.contains(" ")) {
            logger.error("Prefix cannot contain a space.");
            throw new IllegalArgumentException("Prefix cannot contain a space");
        }

        // Save IDiscordClient
        this.client = client;

        // Save default prefix
        this.globalDefaultPrefix = defaultPrefix.toLowerCase();

        // Get all static methods with @BotCommand and create CustomCommand objects
        commands.addAll(new Reflections(packagePrefix, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(BotCommand.class).stream().filter(a -> Modifier.isStatic(a.getModifiers())).map(a -> new CustomCommand(this, a)).collect(Collectors.toList()));

        // Register our command listener
        getClient().getDispatcher().registerListener(new CommandListener(this));

        // Register listener for help command
        getClient().getDispatcher().registerListener(new HelpCommand(this));
        try {
            commands.add(new CustomCommand(this, HelpCommand.class.getMethod("helpCommand", CommandContext.class)));
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register help command.");
        }

        // Sort all registered commands for the Help Module
        commands.sort(new CommandComparator());
    }

    /**
     * Get the Discord4J client
     *
     * @return IDiscordClient
     */
    protected IDiscordClient getClient() {
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
    public String getCommandPrefix(IGuild guild) {
        if (guild == null)
            return globalDefaultPrefix;
        return commandPrefixes.getOrDefault(guild.getLongID(), globalDefaultPrefix);
    }

    /**
     * Set the command prefix of a specified guild
     *
     * @param guild         Guild to change the prefix for
     * @param commandPrefix new prefix characters all commands must be prefaced with
     */
    public void setCommandPrefix(IGuild guild, String commandPrefix) {
        // Check prefix for space
        if(commandPrefix.contains(" ")) {
            logger.error("Cannot set guild prefix. Prefix cannot contain a space.");
            throw new IllegalArgumentException("Prefix cannot contain a space");
        }
        commandPrefixes.put(guild.getLongID(), commandPrefix.toLowerCase());
    }

    static class CommandComparator implements Comparator<CustomCommand> {
        public int compare(CustomCommand c1, CustomCommand c2) {
            return (c1.getModule() + " " + String.join(" ", c1.getCommands())).compareTo(c2.getModule() + " " + String.join(" ", c2.getCommands()));
        }
    }
}
