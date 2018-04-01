package com.discordbolt.api.command;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Tony on 2/16/2017.
 */
public class CommandContext {

    private IMessage message;
    private List<String> arguments;
    private CustomCommand customCommand;

    protected CommandContext(IMessage message, CustomCommand customCommand) {
        this.message = message;
        this.customCommand = customCommand;
        arguments = Arrays.asList(getMessageContent().substring(this.getPrefix().length(), getMessageContent().length()).split(" "));
    }

    public IDiscordClient getClient() { return message.getClient(); }

    public String getPrefix() { return customCommand.getCommandManager().getCommandPrefix(message.getGuild()); }

    public IMessage getMessage() {
        return message;
    }

    public IUser getAuthor() {
        return message.getAuthor();
    }

    public String getAuthorDisplayName() {
        return getAuthor().getDisplayName(getGuild());
    }

    public IGuild getGuild() {
        return getMessage().getGuild();
    }

    public IChannel getChannel() {
        return message.getChannel();
    }

    public boolean isPrivateMessage() {
        return getChannel().isPrivate();
    }

    public String getMessageContent() {
        return message.getContent();
    }

    public String getUserBaseCommand() {
        return arguments.get(0);
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getArgument(int index) {
        return arguments.get(index);
    }

    public int getArgCount() {
        return getArguments().size();
    }

    public String combineArgs(int lowIndex, int highIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append(getArgument(lowIndex));
        for (int i = lowIndex + 1; i <= highIndex; i++)
            sb.append(' ').append(getArgument(i));

        return sb.toString();
    }

    public IMessage replyWith(String message) {
        return RequestBuffer.request(() -> {
            return getChannel().sendMessage(message);
        }).get();
    }

    public IMessage replyWith(EmbedObject embedObject) {
        return RequestBuffer.request(() -> {
            return getChannel().sendMessage(embedObject);
        }).get();
    }

    public IMessage replyWith(String message, EmbedObject embedObject) {
        return RequestBuffer.request( () -> {
            return getChannel().sendMessage(message, embedObject);
        }).get();
    }

    public void sendUsage() {
        replyWith(customCommand.getUsage(getGuild()));
    }
}
