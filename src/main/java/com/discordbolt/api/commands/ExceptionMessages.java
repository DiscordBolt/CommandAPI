package com.discordbolt.api.commands;

public interface ExceptionMessages {

    String API_LIMIT = "Sending Discord too many requests. Rate limit hit.";
    String PERMISSION_DENIED = "You do not have permission for this command!";
    String BOT_PERMISSION_DENIED = "I do not have permission to perform this action!";
    String COMMAND_PROCESS_EXCEPTION = "An error has occurred while processing your command. Please try again later.";
    String BAD_STATE = "I'm sorry Dave, I'm afraid I can't do that";
    String EXECUTE_IN_GUILD = "You must execute this command in a guild.";
    String TOO_FEW_ARGUMENTS = "Your command had too few arguments.";
    String TOO_MANY_ARGUMENTS = "Your command had too many arguments.";
    String INVALID_CHANNEL = "This command can not be executed in this channel!";

    @Deprecated
    String INCORRECT_USAGE = "Your command did not match expected input. Please check !Help for usage.";
}