package com.discordbolt.api.commands;

import discord4j.core.spec.EmbedCreateSpec;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

class HelpCommand extends CustomCommand {

    private static String[] command = {"help"};

    private CommandManager manager;

    HelpCommand(CommandManager manager) {
        super(command);
        this.manager = manager;
        super.setSecret(true);
        super.setAliases("h");
    }

    @Override
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

        int fieldCount = 0;
        EmbedCreateSpec embed = new EmbedCreateSpec();
        embed.setColor(new Color(36, 153, 153).getRGB());

        StringBuilder sb = new StringBuilder();
        for (String module : modules) {
            // Discord only allows 25 fields in an embed
            if (fieldCount > 25)
                continue;
            sb.setLength(0);

            // Get all commands of this module
            manager.getCommands().stream().filter(c -> c.getModule().equals(module)).forEach(command -> {
                if (command.isSecret()) {
                    return;
                }

                sb.append('`').append(commandPrefix).append(String.join(" ", command.getCommands())).append("` | ").append(command.getDescription()).append('\n');
            });

            // Discord only allows field descriptions to be 1024 characters
            if (sb.length() > 1024) {
                sb.setLength(1024);
            }

            if (module.length() == 0 || sb.length() == 0) {
                continue;
            }

            fieldCount++;
            embed.addField(module, sb.toString(), false);
        }
        if (fieldCount > 0) {
            cc.replyWith(embed).subscribe();
        } else {
            cc.replyWith("No available commands.").subscribe();
        }
    }
}
