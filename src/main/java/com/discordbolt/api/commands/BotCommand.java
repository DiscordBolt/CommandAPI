package com.discordbolt.api.commands;

import discord4j.core.object.util.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {

    /**
     * The commands and optional command arguments that map to a specific action
     *
     * @return
     */
    String[] command();

    /**
     * The description of what the command does
     *
     * @return
     */
    String description();

    /**
     * The description of the syntax of the command
     *
     * @return
     */
    String usage();

    /**
     * The module the command falls into
     * This is used to sort commands in !Help
     *
     * @return
     */
    String module();

    /**
     * A list of alternative commands that take place of the command at index 0
     *
     * @return
     */
    String[] aliases() default {};

    /**
     * A list of channel names that this command is allowed to execute in
     *
     * @return
     */
    String[] allowedChannelNames() default {};

    /**
     * A list of channel names that this command is not allowed to execute in
     *
     * @return
     */
    String[] disallowedChannelNames() default {};

    /**
     * Required permissions a user executing a command must have
     *
     * @return
     */
    Permission[] permissions() default {};

    /**
     * The required number (or range) or args.
     * This should be a single int or two ints
     *
     * @return
     */
    int[] args() default {};

    /**
     * Should this command be hidden from the !Help command?
     *
     * @return
     */
    boolean secret() default false;

    /**
     * Allow executing this command in direct messages
     *
     * @return
     */
    boolean allowDM() default false;

    /**
     * Delete the message that invoked this command.
     * Note: Sent messages during executing of this command will NOT be deleted.
     *
     * @return
     */
    boolean deleteCommandMessage() default false;
}
