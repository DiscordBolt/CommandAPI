package com.discordbolt.api.commands.exceptions;

import com.discordbolt.api.commands.ExceptionMessages;

/**
 * Created by Tony on 4/19/2017.
 */
public class CommandRuntimeException extends RuntimeException {

    public CommandRuntimeException() {
        super(ExceptionMessages.COMMAND_PROCESS_EXCEPTION);
    }

    public CommandRuntimeException(String message) {
        super(message);
    }
}
