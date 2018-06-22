package com.discordbolt.api.commands;

import discord4j.core.object.util.Permission;

public abstract class Command {

    private String[] command;
    private String description;
    private String usage;
    private String module;

    private String[] aliases = {};
    private long[] channelWhitelist = {};
    private String[] channelNameWhitelist = {};
    private long[] channelBlacklist = {};
    private String[] channelNameBlacklist = {};
    private Permission[] permissions = {};
    private int[] args = {};
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

    public Command setCommand(String[] command) {
        this.command = command;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Command setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUsage() {
        return usage;
    }

    public Command setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public String getModule() {
        return module;
    }

    public Command setModule(String module) {
        this.module = module;
        return this;
    }

    public String[] getAliases() {
        return aliases;
    }

    public Command setAliases(String[] aliases) {
        this.aliases = aliases;
        return this;
    }

    public long[] getChannelWhitelist() {
        return channelWhitelist;
    }

    public Command setChannelWhitelist(long[] channelWhitelist) {
        this.channelWhitelist = channelWhitelist;
        return this;
    }

    public String[] getChannelNameWhitelist() {
        return channelNameWhitelist;
    }

    public Command setChannelNameWhitelist(String[] channelNameWhitelist) {
        this.channelNameWhitelist = channelNameWhitelist;
        return this;
    }

    public long[] getChannelBlacklist() {
        return channelBlacklist;
    }

    public Command setChannelBlacklist(long[] channelBlacklist) {
        this.channelBlacklist = channelBlacklist;
        return this;
    }

    public String[] getChannelNameBlacklist() {
        return channelNameBlacklist;
    }

    public Command setChannelNameBlacklist(String[] channelNameBlacklist) {
        this.channelNameBlacklist = channelNameBlacklist;
        return this;
    }

    public Permission[] getPermissions() {
        return permissions;
    }

    public Command setPermissions(Permission[] permissions) {
        this.permissions = permissions;
        return this;
    }

    public int[] getArgs() {
        return args;
    }

    public Command setArgs(int[] args) {
        this.args = args;
        return this;
    }

    public boolean isSecret() {
        return secret;
    }

    public Command setSecret(boolean secret) {
        this.secret = secret;
        return this;
    }

    public boolean isAllowDM() {
        return allowDM;
    }

    public Command setAllowDM(boolean allowDM) {
        this.allowDM = allowDM;
        return this;
    }

    public boolean isDeleteCommandMessage() {
        return deleteCommandMessage;
    }

    public Command setDeleteCommandMessage(boolean deleteCommandMessage) {
        this.deleteCommandMessage = deleteCommandMessage;
        return this;
    }
}
