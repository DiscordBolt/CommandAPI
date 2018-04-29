package com.discordbolt.commandapi;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class CommandAPI {

    private final Logger LOGGER = Loggers.getLogger(CommandAPI.class);
    private final String DEFAULT_COMMAND_PREFIX = "!";

    private DiscordClient client;
    private final List<CustomCommand> commands = new ArrayList<>();
    private final Map<Snowflake, String> commandPrefixes = new HashMap<>();

    public CommandAPI(DiscordClient client, String packagePrefix) {
        LOGGER.info("Starting CommandAPI v{}", getVersion());

        this.client = client;

        // Register all commands annotated with @BotCommand
        commands.addAll(new Reflections(packagePrefix, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(BotCommand.class).stream().filter(a -> Modifier.isStatic(a.getModifiers())).map(a -> new CustomCommand(this, a)).collect(Collectors.toList()));

        // Register command listener
       // getClient().getEventDispatcher().on(new CommandListener());

        // Register Help Command
        // TODO register help command

        commands.sort(new Comparator<CustomCommand>() {
            @Override
            public int compare(CustomCommand c1, CustomCommand c2) {
                return 0;//(c1.getModule() + " " + String.join(" ", c1.getCommands())).compareTo(c2.getModule() + " " + String.join(" ", c2.getCommands()));
            }
        });

    }

    public DiscordClient getClient() {
        return client;
    }

    public String getVersion() {
        try {
            Properties p = new Properties();
            p.load(new FileInputStream(new File("CommandAPI.properties")));
            return p.getProperty("VERSION");
        } catch (IOException e) {
            LOGGER.error("Unable to load CommandAPI.properties", e);
            return "DEVELOPMENT";
        }
    }

    public List<CustomCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public String getCommandPrefix(Guild guild) {
        if (guild == null)
            return DEFAULT_COMMAND_PREFIX;
        return commandPrefixes.getOrDefault(guild.getId(), DEFAULT_COMMAND_PREFIX);
    }

    public void setCommandPrefix(Guild guild, String prefix) {
        commandPrefixes.put(guild.getId(), prefix);
        //TODO update value in database (via storage interface?)
    }
}
