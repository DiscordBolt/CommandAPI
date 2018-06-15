package com.discordbolt.api.commands;

import discord4j.core.object.util.Permission;

public abstract class Command {

    private String[] command;
    private String description;
    private String usage;
    private String module;

    private String[] aliases;
    private long[] channelWhitelist;
    private String[] channelNameWhitelist;
    private long[] channelBlacklist;
    private String[] channelNameBlacklist;
    private Permission[] permissions;
    private int[] args;
    private boolean secret;
    private boolean allowDM;
    private boolean deleteCommandMessage;

    public Command(String[] command, String description, String usage, String module) {
        this.command = command;
        this.description = description;
        this.usage = usage;
        this.module = module;
    }

    public abstract void execute(CommandContext cc);

    public String[] getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getModule() {
        return module;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public long[] getChannelWhitelist() {
        return channelWhitelist;
    }

    public void setChannelWhitelist(long[] channelWhitelist) {
        this.channelWhitelist = channelWhitelist;
    }

    public String[] getChannelNameWhitelist() {
        return channelNameWhitelist;
    }

    public void setChannelNameWhitelist(String[] channelNameWhitelist) {
        this.channelNameWhitelist = channelNameWhitelist;
    }

    public long[] getChannelBlacklist() {
        return channelBlacklist;
    }

    public void setChannelBlacklist(long[] channelBlacklist) {
        this.channelBlacklist = channelBlacklist;
    }

    public String[] getChannelNameBlacklist() {
        return channelNameBlacklist;
    }

    public void setChannelNameBlacklist(String[] channelNameBlacklist) {
        this.channelNameBlacklist = channelNameBlacklist;
    }

    public Permission[] getPermissions() {
        return permissions;
    }

    public void setPermissions(Permission[] permissions) {
        this.permissions = permissions;
    }

    public int[] getArgs() {
        return args;
    }

    public void setArgs(int[] args) {
        this.args = args;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public boolean isAllowDM() {
        return allowDM;
    }

    public void setAllowDM(boolean allowDM) {
        this.allowDM = allowDM;
    }

    public boolean isDeleteCommandMessage() {
        return deleteCommandMessage;
    }

    public void setDeleteCommandMessage(boolean deleteCommandMessage) {
        this.deleteCommandMessage = deleteCommandMessage;
    }
}
