package com.discordbolt.api.commands;

import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.discordbolt.api.commands.ValidityCheck.CheckResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ValidityCheckTest {

    private CustomCommand customCommand;
    private CommandContext commandContext;

    private PrivateChannel directMessageChannel;
    private TextChannel textChannel, textChannel2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Print out actually useful debug information for reactor stacktraces
        Hooks.onOperatorDebug();
    }

    @Before
    public void setUp() throws Exception {
        customCommand = Mockito.mock(CustomCommand.class);
        commandContext = Mockito.mock(CommandContext.class);
        directMessageChannel = Mockito.mock(PrivateChannel.class);
        textChannel = Mockito.mock(TextChannel.class);
        textChannel2 = Mockito.mock(TextChannel.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testChannelDM() {
        // Call the actual method for determining if the message was a direct message or not
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();

        // Allow command in DM
        Mockito.when(customCommand.allowDM()).thenReturn(true);
        // Test command in DM
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(), equalTo(CheckResult.VALID));
        // Test command in normal channel
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Deny the command in DM
        Mockito.when(customCommand.allowDM()).thenReturn(false);
        // Test command in DM
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(), equalTo(CheckResult.DM_NOT_ALLOWED));
        // Test command in normal channel
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(), equalTo(CheckResult.VALID));
    }

    @Test
    public void testChannelBlacklist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getId()).thenReturn(Snowflake.of(123456L));
        Mockito.when(textChannel2.getId()).thenReturn(Snowflake.of(666666L));

        Set<Long> channelBlacklist = new HashSet<>(Collections.singletonList(textChannel.getId().asLong()));
        Set<Long> unrelatedChannelBlacklist = new HashSet<>(Collections.singletonList(textChannel2.getId().asLong()));
        Set<Long> emptyBlacklist = new HashSet<>();

        // No channels on blacklist
        Mockito.when(customCommand.getChannelBlacklist()).thenReturn(emptyBlacklist);
        assertThat(ValidityCheck.channelBlacklist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Channel was on blacklist
        Mockito.when(customCommand.getChannelBlacklist()).thenReturn(channelBlacklist);
        assertThat(ValidityCheck.channelBlacklist(customCommand, commandContext).block(), equalTo(CheckResult.CHANNEL_ON_BLACKLIST));

        // Unrelated channel on blacklist
        Mockito.when(customCommand.getChannelBlacklist()).thenReturn(unrelatedChannelBlacklist);
        assertThat(ValidityCheck.channelBlacklist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));
    }

    @Test
    public void testChannelNameBlacklist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getName()).thenReturn("general");
        Mockito.when(textChannel2.getName()).thenReturn("music");

        Set<String> channelBlacklist = new HashSet<>(Collections.singletonList(textChannel.getName()));
        Set<String> unrelatedChannelBlacklist = new HashSet<>(Collections.singletonList(textChannel2.getName()));
        Set<String> emptyBlacklist = new HashSet<>();

        // No channels on blacklist
        Mockito.when(customCommand.getChannelNameBlacklist()).thenReturn(emptyBlacklist);
        assertThat(ValidityCheck.channelNameBlacklist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Channel was on blacklist
        Mockito.when(customCommand.getChannelNameBlacklist()).thenReturn(channelBlacklist);
        assertThat(ValidityCheck.channelNameBlacklist(customCommand, commandContext).block(), equalTo(CheckResult.CHANNEL_ON_BLACKLIST));

        // Unrelated channel on blacklist
        Mockito.when(customCommand.getChannelNameBlacklist()).thenReturn(unrelatedChannelBlacklist);
        assertThat(ValidityCheck.channelNameBlacklist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));
    }

    @Test
    public void testChannelWhitelist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getId()).thenReturn(Snowflake.of(123456L));
        Mockito.when(textChannel2.getId()).thenReturn(Snowflake.of(666666L));

        Set<Long> channelWhitelist = new HashSet<>(Collections.singletonList(textChannel.getId().asLong()));
        Set<Long> unrelatedChannelWhitelist = new HashSet<>(Collections.singletonList(textChannel2.getId().asLong()));
        Set<Long> emptyWhitelist = new HashSet<>();

        // No channels on whitelist
        Mockito.when(customCommand.getChannelWhitelist()).thenReturn(emptyWhitelist);
        assertThat(ValidityCheck.channelWhitelist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Channel was on whitelist
        Mockito.when(customCommand.getChannelWhitelist()).thenReturn(channelWhitelist);
        assertThat(ValidityCheck.channelWhitelist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Unrelated channel on whitelist
        Mockito.when(customCommand.getChannelWhitelist()).thenReturn(unrelatedChannelWhitelist);
        assertThat(ValidityCheck.channelWhitelist(customCommand, commandContext).block(), equalTo(CheckResult.CHANNEL_NOT_ON_WHITELIST));
    }

    @Test
    public void testChannelNameWhitelist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getName()).thenReturn("general");
        Mockito.when(textChannel2.getName()).thenReturn("music");

        Set<String> channelWhitelist = new HashSet<>(Collections.singletonList(textChannel.getName()));
        Set<String> unrelatedChannelWhitelist = new HashSet<>(Collections.singletonList(textChannel2.getName()));
        Set<String> emptyWhitelist = new HashSet<>();

        // No channels on whitelist
        Mockito.when(customCommand.getChannelNameWhitelist()).thenReturn(emptyWhitelist);
        assertThat(ValidityCheck.channelNameWhitelist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Channel was on whitelist
        Mockito.when(customCommand.getChannelNameWhitelist()).thenReturn(channelWhitelist);
        assertThat(ValidityCheck.channelNameWhitelist(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Unrelated channel on whitelist
        Mockito.when(customCommand.getChannelNameWhitelist()).thenReturn(unrelatedChannelWhitelist);
        assertThat(ValidityCheck.channelNameWhitelist(customCommand, commandContext).block(), equalTo(CheckResult.CHANNEL_NOT_ON_WHITELIST));
    }

    @Test
    public void testArgumentLowerBound() {
        Mockito.when(commandContext.getArgCount()).thenReturn(5);

        // No lower argument bound (valid)
        Mockito.when(customCommand.getMinimumArgCount()).thenReturn(Optional.empty());
        assertThat(ValidityCheck.argumentLowerBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Above lower argument bound (valid)
        Mockito.when(customCommand.getMinimumArgCount()).thenReturn(Optional.of(3));
        assertThat(ValidityCheck.argumentLowerBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Below lower argument bound (fail)
        Mockito.when(customCommand.getMinimumArgCount()).thenReturn(Optional.of(10));
        assertThat(ValidityCheck.argumentLowerBound(customCommand, commandContext).block(), equalTo(CheckResult.TOO_FEW_ARGUMENTS));
    }

    @Test
    public void testArgumentUpperBound() {
        Mockito.when(commandContext.getArgCount()).thenReturn(20);

        // No upper argument bound (valid)
        Mockito.when(customCommand.getMaximumArgCount()).thenReturn(Optional.empty());
        assertThat(ValidityCheck.argumentUpperBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Below upper argument bound (valid)
        Mockito.when(customCommand.getMaximumArgCount()).thenReturn(Optional.of(25));
        assertThat(ValidityCheck.argumentUpperBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Above upper argument bound (fail)
        Mockito.when(customCommand.getMaximumArgCount()).thenReturn(Optional.of(10));
        assertThat(ValidityCheck.argumentUpperBound(customCommand, commandContext).block(), equalTo(CheckResult.TOO_MANY_ARGUMENTS));
    }

    @Test
    public void testPermission() {
    }
}