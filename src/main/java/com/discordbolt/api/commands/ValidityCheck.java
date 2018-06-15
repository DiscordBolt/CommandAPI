package com.discordbolt.api.commands;

import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class ValidityCheck {

    private static final Mono<CheckResult> VALID_CHECK = Mono.just(CheckResult.VALID);

    protected enum CheckResult {
        VALID(""), DM_NOT_ALLOWED(ExceptionMessages.EXECUTE_IN_GUILD), CHANNEL_ON_BLACKLIST(ExceptionMessages.INVALID_CHANNEL), CHANNEL_NOT_ON_WHITELIST(ExceptionMessages.INVALID_CHANNEL), TOO_FEW_ARGUMENTS(ExceptionMessages.TOO_FEW_ARGUMENTS), TOO_MANY_ARGUMENTS(ExceptionMessages.TOO_MANY_ARGUMENTS), INVALID_PERMISSION(ExceptionMessages.PERMISSION_DENIED);

        private final String message;

        CheckResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    protected static Mono<CheckResult> channelDM(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.isDirectMessage().map(isDM -> command.allowDM() || !isDM)).switchIfEmpty(Mono.just(CheckResult.DM_NOT_ALLOWED));
    }

    protected static Mono<CheckResult> channelBlacklist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel().map(MessageChannel::getId).map(Snowflake::asLong).map(channelID -> !command.getChannelBlacklist().contains(channelID))).switchIfEmpty(Mono.just(CheckResult.CHANNEL_ON_BLACKLIST));
    }

    protected static Mono<CheckResult> channelNameBlacklist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel().ofType(TextChannel.class).map(TextChannel::getName).map(channelName -> !command.getChannelNameBlacklist().contains(channelName)))
                          .switchIfEmpty(Mono.just(CheckResult.CHANNEL_ON_BLACKLIST));
    }

    protected static Mono<CheckResult> channelWhitelist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel().map(MessageChannel::getId).map(Snowflake::asLong).map(channelID -> command.getChannelWhitelist().isEmpty() || command.getChannelWhitelist().contains(channelID)))
                          .switchIfEmpty(Mono.just(CheckResult.CHANNEL_NOT_ON_WHITELIST));
    }

    protected static Mono<CheckResult> channelNameWhitelist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel().ofType(TextChannel.class).map(TextChannel::getName).map(channelName -> command.getChannelNameWhitelist().isEmpty() || command.getChannelNameWhitelist().contains(channelName)))
                          .switchIfEmpty(Mono.just(CheckResult.CHANNEL_NOT_ON_WHITELIST));
    }

    protected static Mono<CheckResult> argumentLowerBound(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filter(r -> commandContext.getArgCount() >= command.getMinimumArgCount().orElse(0)).switchIfEmpty(Mono.just(CheckResult.TOO_FEW_ARGUMENTS));
    }

    protected static Mono<CheckResult> argumentUpperBound(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filter(r -> commandContext.getArgCount() <= command.getMaximumArgCount().orElse(Integer.MAX_VALUE)).switchIfEmpty(Mono.just(CheckResult.TOO_MANY_ARGUMENTS));
    }

    protected static Mono<CheckResult> permission(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK; //TODO actually check permissions
    }
}
