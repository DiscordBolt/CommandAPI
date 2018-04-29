package com.discordbolt.commandapi;

import discord4j.core.object.entity.Message;

import java.util.Arrays;
import java.util.List;

public class CommandContext {

    public static CommandAPI api;

    private Message message;
    private List<String> arguments;
    private CustomCommand customCommand;

    protected CommandContext(CommandAPI api, CustomCommand customCommand, Message message) {
        this.message = message;
        arguments = Arrays.asList(message.getContent().get().substring(api.getCommandPrefix(message.getGuild().block()).length()));
    }
}
