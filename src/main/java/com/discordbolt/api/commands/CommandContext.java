package com.discordbolt.api.commands;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Arrays;
import java.util.List;
import reactor.core.publisher.Mono;

public class CommandContext {

    private Message message;
    private List<String> arguments;
    private CustomCommand customCommand;

    CommandContext(Message message, CustomCommand customCommand) {
        this.message = message;
        getGuild().subscribe(
                guild -> this.arguments = Arrays.asList(getMessageContent().substring(customCommand
                        .getCommandManager().getCommandPrefix(guild).length()).split(" ")));
        this.customCommand = customCommand;
    }

    /**
     * Get a list of strings that represent the command executed. This does not contain user
     * supplied arguments
     */
    public List<String> getCommand() {
        return customCommand.getCommands();
    }

    public Message getMessage() {
        return message;
    }

    public Mono<User> getUser() {
        return message.getAuthor();
    }

    public Mono<Member> getMember() {
        return getMessage().getAuthorAsMember();
    }

    public Mono<Guild> getGuild() {
        return getMessage().getGuild();
    }

    public Mono<MessageChannel> getChannel() {
        return message.getChannel();
    }

    public Mono<Boolean> isDirectMessage() {
        return getChannel().ofType(PrivateChannel.class).hasElement();
    }

    public String getMessageContent() {
        // The message has to have content otherwise the command wouldn't have been fired.
        return message.getContent().get();
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
        for (int i = lowIndex + 1; i <= highIndex; i++) {
            sb.append(' ').append(getArgument(i));
        }

        return sb.toString();
    }

    /**
     * Reply to the command with a given message Note: Make sure to subscribe to the result or no
     * message will be sent.
     *
     * @param message Message to send
     */
    public Mono<Message> replyWith(String message) {
        return getChannel()
                .flatMap(channel -> channel.createMessage(spec -> spec.setContent(message)));
    }

    /**
     * Reply to the command with a given embed Note: Make sure to subscribe to the result or no
     * embed will be sent.
     *
     * @param embed Embed to send
     */
    public Mono<Message> replyWith(EmbedCreateSpec embed) {
        return getChannel().flatMap(channel -> channel.createMessage(spec -> spec.setEmbed(embed)));
    }

    /**
     * Reply to the command with a given message and embed Note: Make sure to subscribe to the
     * result or no message will be sent.
     *
     * @param message Message to send
     * @param embed Embed to send
     */
    public Mono<Message> replyWith(String message, EmbedCreateSpec embed) {
        return getChannel().flatMap(
                channel -> channel.createMessage(spec -> spec.setContent(message).setEmbed(embed)));
    }

    /**
     * Reply to the command with the usage Note: Make sure to subscribe to the result or no message
     * will be sent.
     */
    public Mono<Message> sendUsage() {
        return getGuild().map(customCommand::getUsage).flatMap(this::replyWith);
    }
}
