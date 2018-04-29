package com.discordbolt.commandapi;

import discord4j.core.object.util.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {
    /**
     * The array of arguments the command is known by.
     * Example:
     * !Ping -> command = ["Ping"]
     * !Volume Set -> command = ["Volume", "Set"]
     */
    String[] command();

    /**
     * The description of what the command does
     * Used in !Help command
     */
    String description();

    /**
     * The usage of the command with all arguments
     * Used in !Help command
     */
    String usage();

    /**
     * The module this command belongs to
     * Used in !Help command to group similar commands
     */
    String module();

    /**
     * An optional list of aliases instead of command[0]
     */
    String[] aliases() default {};

    /**
     * An optional list of channel names this command is allowed to be executed in
     */
    String[] allowedChannels() default {};

    /**
     * An optional list of permissions required to execute this command
     */
    Permission[] permissions() default {};

    /**
     * The exact required number of arguments
     */
    int args() default -1;

    /**
     * The minimum bound of required arguments
     */
    int minArgs() default -1;

    /**
     * The maximum bound of required arguments
     */
    int maxArgs() default -1;

    /**
     * Should the command be hidden on the !Help command
     */
    boolean secret() default false;

    /**
     * Should this command be executable in a direct message
     * Note: if a command requires permissions it must be executed in a guild
     */
    boolean allowDM() default false;

    /**
     * Delete the message that invoked the command?
     */
    boolean deleteCommandMessage() default false;
}
