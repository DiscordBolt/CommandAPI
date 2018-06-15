package com.discordbolt.api.commands.exceptions;

import com.discordbolt.api.commands.ExceptionMessages;

/**
 * Created by Tony on 4/19/2017.
 */
public class CommandException extends Exception {

    public CommandException() {
        super(ExceptionMessages.COMMAND_PROCESS_EXCEPTION);
    }

    public CommandException(String message) {
        super(message);
    }
}
