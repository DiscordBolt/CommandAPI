package com.discordbolt.api.commands;

import static com.discordbolt.api.commands.ValidityCheck.CheckResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ValidityCheckTest {

    private CustomCommand customCommand;
    private CommandContext commandContext;

    private PrivateChannel directMessageChannel;
    private TextChannel textChannel, textChannel2;

    private Member member;
    private Role everyoneRole, moveUserRole, kickBanUserRole;

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
        member = Mockito.mock(Member.class);
        everyoneRole = Mockito.mock(Role.class);
        moveUserRole = Mockito.mock(Role.class);
        kickBanUserRole = Mockito.mock(Role.class);

        Mockito.when(everyoneRole.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(moveUserRole.getPermissions())
                .thenReturn(PermissionSet.of(Permission.MOVE_MEMBERS));
        Mockito.when(kickBanUserRole.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS,
                        Permission.BAN_MEMBERS));
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
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));
        // Test command in normal channel
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));

        // Deny the command in DM
        Mockito.when(customCommand.allowDM()).thenReturn(false);
        // Test command in DM
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(),
                equalTo(CheckResult.DM_NOT_ALLOWED));
        // Test command in normal channel
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        assertThat(ValidityCheck.channelDM(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));
    }

    @Test
    public void testChannelBlacklist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getId()).thenReturn(Snowflake.of(123456L));
        Mockito.when(textChannel2.getId()).thenReturn(Snowflake.of(666666L));

        Set<Long> channelBlacklist = new HashSet<>(
                Collections.singletonList(textChannel.getId().asLong()));
        Set<Long> unrelatedChannelBlacklist = new HashSet<>(
                Collections.singletonList(textChannel2.getId().asLong()));
        Set<Long> emptyBlacklist = new HashSet<>();

        // No channels on blacklist
        Mockito.when(customCommand.getChannelBlacklist()).thenReturn(emptyBlacklist);
        assertThat(ValidityCheck.channelBlacklist(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));

        // Channel was on blacklist
        Mockito.when(customCommand.getChannelBlacklist()).thenReturn(channelBlacklist);
        assertThat(ValidityCheck.channelBlacklist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .CHANNEL_ON_BLACKLIST));

        // Unrelated channel on blacklist
        Mockito.when(customCommand.getChannelBlacklist()).thenReturn(unrelatedChannelBlacklist);
        assertThat(ValidityCheck.channelBlacklist(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));
    }

    @Test
    public void testChannelNameBlacklist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getName()).thenReturn("general");
        Mockito.when(textChannel2.getName()).thenReturn("music");

        Set<String> channelBlacklist = new HashSet<>(
                Collections.singletonList(textChannel.getName()));
        Set<String> unrelatedChannelBlacklist = new HashSet<>(
                Collections.singletonList(textChannel2.getName()));
        Set<String> emptyBlacklist = new HashSet<>();

        // No channels on blacklist
        Mockito.when(customCommand.getChannelNameBlacklist()).thenReturn(emptyBlacklist);
        assertThat(ValidityCheck.channelNameBlacklist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .VALID));

        // Channel was on blacklist
        Mockito.when(customCommand.getChannelNameBlacklist()).thenReturn(channelBlacklist);
        assertThat(ValidityCheck.channelNameBlacklist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .CHANNEL_ON_BLACKLIST));

        // Unrelated channel on blacklist
        Mockito.when(customCommand.getChannelNameBlacklist()).thenReturn(unrelatedChannelBlacklist);
        assertThat(ValidityCheck.channelNameBlacklist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .VALID));
    }

    @Test
    public void testChannelWhitelist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getId()).thenReturn(Snowflake.of(123456L));
        Mockito.when(textChannel2.getId()).thenReturn(Snowflake.of(666666L));

        Set<Long> channelWhitelist = new HashSet<>(
                Collections.singletonList(textChannel.getId().asLong()));
        Set<Long> unrelatedChannelWhitelist = new HashSet<>(
                Collections.singletonList(textChannel2.getId().asLong()));
        Set<Long> emptyWhitelist = new HashSet<>();

        // No channels on whitelist
        Mockito.when(customCommand.getChannelWhitelist()).thenReturn(emptyWhitelist);
        assertThat(ValidityCheck.channelWhitelist(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));

        // Channel was on whitelist
        Mockito.when(customCommand.getChannelWhitelist()).thenReturn(channelWhitelist);
        assertThat(ValidityCheck.channelWhitelist(customCommand, commandContext).block(),
                equalTo(CheckResult.VALID));

        // Unrelated channel on whitelist
        Mockito.when(customCommand.getChannelWhitelist()).thenReturn(unrelatedChannelWhitelist);
        assertThat(ValidityCheck.channelWhitelist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .CHANNEL_NOT_ON_WHITELIST));
    }

    @Test
    public void testChannelNameWhitelist() {
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(textChannel.getName()).thenReturn("general");
        Mockito.when(textChannel2.getName()).thenReturn("music");

        Set<String> channelWhitelist = new HashSet<>(
                Collections.singletonList(textChannel.getName()));
        Set<String> unrelatedChannelWhitelist = new HashSet<>(
                Collections.singletonList(textChannel2.getName()));
        Set<String> emptyWhitelist = new HashSet<>();

        // No channels on whitelist
        Mockito.when(customCommand.getChannelNameWhitelist()).thenReturn(emptyWhitelist);
        assertThat(ValidityCheck.channelNameWhitelist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .VALID));

        // Channel was on whitelist
        Mockito.when(customCommand.getChannelNameWhitelist()).thenReturn(channelWhitelist);
        assertThat(ValidityCheck.channelNameWhitelist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .VALID));

        // Unrelated channel on whitelist
        Mockito.when(customCommand.getChannelNameWhitelist()).thenReturn(unrelatedChannelWhitelist);
        assertThat(ValidityCheck.channelNameWhitelist(customCommand, commandContext).block(),
                equalTo(CheckResult
                        .CHANNEL_NOT_ON_WHITELIST));
    }

    @Test
    public void testArgumentLowerBound() {
        Mockito.when(commandContext.getArgCount()).thenReturn(5);

        // No lower argument bound (valid)
        Mockito.when(customCommand.getMinArgCount()).thenReturn(0);
        assertThat(ValidityCheck.argumentLowerBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Above lower argument bound (valid)
        Mockito.when(customCommand.getMinArgCount()).thenReturn(3);
        assertThat(ValidityCheck.argumentLowerBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Below lower argument bound (fail)
        Mockito.when(customCommand.getMinArgCount()).thenReturn(10);
        assertThat(ValidityCheck.argumentLowerBound(customCommand, commandContext).block(), equalTo(CheckResult.TOO_FEW_ARGUMENTS));
    }

    @Test
    public void testArgumentUpperBound() {
        Mockito.when(commandContext.getArgCount()).thenReturn(20);

        // No upper argument bound (valid)
        Mockito.when(customCommand.getMaxArgCount()).thenReturn(Integer.MAX_VALUE);
        assertThat(ValidityCheck.argumentUpperBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Below upper argument bound (valid)
        Mockito.when(customCommand.getMaxArgCount()).thenReturn(25);
        assertThat(ValidityCheck.argumentUpperBound(customCommand, commandContext).block(), equalTo(CheckResult.VALID));

        // Above upper argument bound (fail)
        Mockito.when(customCommand.getMaxArgCount()).thenReturn(10);
        assertThat(ValidityCheck.argumentUpperBound(customCommand, commandContext).block(), equalTo(CheckResult.TOO_MANY_ARGUMENTS));
    }

    /**
     * Command has no required permissions text channel user no permissions
     */
    @Test
    public void testPermission1() {
        Mockito.when(customCommand.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(everyoneRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has no required permissions text channel user has single permission
     */
    @Test
    public void testPermission2() {
        Mockito.when(customCommand.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(moveUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has no required permissions text channel user has multiple permissions
     */
    @Test
    public void testPermission3() {
        Mockito.when(customCommand.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(kickBanUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has no required permissions dm user no permissions
     */
    @Test
    public void testPermission4() {
        Mockito.when(customCommand.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(everyoneRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has no required permissions dm user has single permission
     */
    @Test
    public void testPermission5() {
        Mockito.when(customCommand.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(moveUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has no required permissions dm user has multiple permissions
     */
    @Test
    public void testPermission6() {
        Mockito.when(customCommand.getPermissions()).thenReturn(PermissionSet.none());
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(kickBanUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has 1 required permission text channel user no permissions
     */
    @Test
    public void testPermission7() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.MOVE_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(everyoneRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.INVALID_PERMISSION)
                .expectComplete()
                .verify();
    }

    /**
     * Command has 1 required permission text channel user single permission
     */
    @Test
    public void testPermission8() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.MOVE_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(moveUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has 1 required permission text channel user multiple permissions
     */
    @Test
    public void testPermission9() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(kickBanUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has 1 required permission dm user no permission
     */
    @Test
    public void testPermission10() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.MOVE_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(everyoneRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.DM_NOT_ALLOWED)
                .expectComplete()
                .verify();
    }

    /**
     * Command has 1 required permission dm user single permission
     */
    @Test
    public void testPermission11() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.MOVE_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(moveUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.DM_NOT_ALLOWED)
                .expectComplete()
                .verify();
    }

    /**
     * Command has 1 required permission dm user multiple permissions
     */
    @Test
    public void testPermission12() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.MOVE_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(kickBanUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.DM_NOT_ALLOWED)
                .expectComplete()
                .verify();
    }

    /**
     * Command has >1 required permission text channel user no permission
     */
    @Test
    public void testPermission13() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS, Permission
                        .BAN_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(everyoneRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.INVALID_PERMISSION)
                .expectComplete()
                .verify();
    }

    /**
     * Command has >1 required permission text channel user single permission
     */
    @Test
    public void testPermission14() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS, Permission
                        .BAN_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(moveUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.INVALID_PERMISSION)
                .expectComplete()
                .verify();
    }

    /**
     * Command has >1 required permission text channel user multiple permissions
     */
    @Test
    public void testPermission15() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS, Permission
                        .BAN_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(textChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(kickBanUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.VALID)
                .expectComplete()
                .verify();
    }

    /**
     * Command has >1 required permission dm user no permission
     */
    @Test
    public void testPermission16() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS, Permission
                        .BAN_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(everyoneRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.DM_NOT_ALLOWED)
                .expectComplete()
                .verify();
    }

    /**
     * Command has >1 required permission dm user single permission
     */
    @Test
    public void testPermission17() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS, Permission
                        .BAN_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(moveUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.DM_NOT_ALLOWED)
                .expectComplete()
                .verify();
    }

    /**
     * Command has >1 required permission dm user multiple permissions
     */
    @Test
    public void testPermission18() {
        Mockito.when(customCommand.getPermissions())
                .thenReturn(PermissionSet.of(Permission.KICK_MEMBERS, Permission
                        .BAN_MEMBERS));
        Mockito.when(commandContext.getChannel()).thenReturn(Mono.just(directMessageChannel));
        Mockito.when(commandContext.isDirectMessage()).thenCallRealMethod();
        Mockito.when(commandContext.getMember()).thenReturn(Mono.just(member));
        Mockito.when(member.getRoles()).thenReturn(Flux.just(kickBanUserRole));

        StepVerifier.create(ValidityCheck.permission(customCommand, commandContext))
                .expectNext(CheckResult.DM_NOT_ALLOWED)
                .expectComplete()
                .verify();
    }
}