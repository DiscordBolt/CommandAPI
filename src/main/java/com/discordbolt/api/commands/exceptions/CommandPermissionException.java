package com.discordbolt.api.commands.exceptions;

import com.discordbolt.api.commands.ExceptionMessages;

/**
 * Created by Tony on 4/19/2017.
 */
public class CommandPermissionException extends CommandException {

    public CommandPermissionException() {
        super(ExceptionMessages.PERMISSION_DENIED);
    }

    public CommandPermissionException(String message) {
        super(message);
    }
}
