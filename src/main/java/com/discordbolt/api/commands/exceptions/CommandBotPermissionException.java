package com.discordbolt.api.commands.exceptions;

import com.discordbolt.api.commands.ExceptionMessages;

/**
 * Created by Tony on 4/19/2017.
 */
public class CommandBotPermissionException extends CommandException {

    public CommandBotPermissionException() {
        super(ExceptionMessages.BOT_PERMISSION_DENIED);
    }

    public CommandBotPermissionException(String message) {
        super(message);
    }
}
