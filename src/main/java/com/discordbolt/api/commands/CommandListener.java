package com.discordbolt.api.commands;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class CommandListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    private CommandManager manager;

    protected CommandListener(CommandManager manager, DiscordClient client) {
        this.manager = manager;

        client.getEventDispatcher()
              .on(MessageCreateEvent.class)
              .map(MessageCreateEvent::getMessage)
              .filterWhen(message -> message.getAuthor().map(author -> !author.isBot()))
              .filter(message -> message.getContent().isPresent())
              .filterWhen(message -> message.getGuild().map(guild -> message.getContent().get().length() > manager.getCommandPrefix(guild).length()))
              .subscribe(this::onCommand);
    }

    private void onCommand(Message message) {
        Mono.just(message)
            .filterWhen(msg -> msg.getGuild().map(manager::getCommandPrefix).map(prefix -> msg.getContent().get().startsWith(prefix))).doOnNext(thingy -> LOGGER.info(thingy.getContent().get()))
            .map(msg -> msg.getGuild()
                           .map(manager::getCommandPrefix)
                           .map(prefix -> msg.getContent().get().substring(prefix.length()))
                           .map(rawCommand -> manager.getCommands()
                                                     .stream()
                                                     .filter(command -> command.getCommands().size() <= rawCommand.split(" ").length)
                                                     .filter(command -> matches(command, message.getContent().get()))
                                                     .reduce((first, second) -> second)
                                                     .orElse(null))
                           .filter(Objects::nonNull).doOnNext(thingy -> LOGGER.info(thingy.getCommands().toString()))
                           .doOnNext(command -> command.preexec(message)))
            .subscribe();
    }

    private boolean matches(CustomCommand customCommand, String userCommand) {
        String userBaseCommand = userCommand.substring(1, userCommand.indexOf(" ") > 0 ? userCommand.indexOf(" ") : userCommand.length());

        for (int i = 0; i < customCommand.getCommands().size(); i++) {
            if (i == 0) {  // Checking the base command
                if (!(customCommand.getBaseCommand().equalsIgnoreCase(userBaseCommand) || (customCommand.getAliases().size() > 0 && customCommand.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(userBaseCommand)))))
                    return false;
            } else {  // Check the sub commands
                if (!customCommand.getCommand(i).equalsIgnoreCase(userCommand.split(" ")[i]))
                    return false;
            }
        }
        return true;
    }
}
