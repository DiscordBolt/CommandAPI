package com.discordbolt.api.commands;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpCommand.class);

    private static String[] command = {"help"};
    private static String description = "View all commands";
    private static String usage = "Help";
    private static String module = "Commands";

    private CommandManager manager;

    HelpCommand(CommandManager manager) {
        super(command, description, usage, module);
        this.manager = manager;
        super.setSecret(true);
        super.setAliases(new String[]{"h"});
    }

    public void execute(CommandContext cc) {
        List<String> modules = manager.getCommands().stream().map(CustomCommand::getModule).distinct().collect(Collectors.toList());

        if (cc.getArgCount() > 1) {
            String userRequestedModule = cc.combineArgs(1, cc.getArgCount() - 1);
            modules = modules.stream().filter(s -> s.equalsIgnoreCase(userRequestedModule)).collect(Collectors.toList());
            if (modules.size() < 1) {
                cc.replyWith("No modules found matching \"" + userRequestedModule + "\".").subscribe();
                return;
            }
        }

        String commandPrefix = cc.getGuild().map(manager::getCommandPrefix).block();


        boolean send = false;
        EmbedCreateSpec embed = new EmbedCreateSpec();
        embed.setColor(new Color(36, 153, 153).getRGB());

        StringBuilder sb = new StringBuilder();
        for (String module : modules) {
            sb.setLength(0);

            for (CustomCommand command : manager.getCommands().stream().filter(c -> c.getModule().equals(module)).collect(Collectors.toList())) {
                // TODO Do not block on this and make it better
                Boolean hasPermission = cc.getMember().flatMapMany(Member::getRoles).map(Role::getPermissions).filter(permissions -> permissions.containsAll(command.getPermissions())).hasElements().block();

                if (hasPermission == null || hasPermission.equals(Boolean.FALSE))
                    continue;
                if (command.isSecret())
                    continue;

                sb.append('`').append(commandPrefix).append(String.join(" ", command.getCommands())).append("` | ").append(command.getDescription()).append('\n');
            }
            if (sb.length() > 1024)
                sb.setLength(1024);

            //if (embed.getTotalVisibleCharacters() + sb.length() + module.length() >= 6000)
            //    continue;

            if (module.length() == 0 || sb.length() == 0) {
                continue;
            }

            send = true;
            embed.addField(module, sb.toString(), false);
        }
        if (send) {
            embed.setTitle("Command List");
            embed.setDescription("description");



            cc.replyWith(embed).subscribe(Message::getGuild, throwable -> LOGGER.error("embed error", throwable));

        }
        else
            cc.replyWith("No available commands.").subscribe();
    }
}
