/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import dev.chojo.aether.discordoauth.pojo.DiscordGuild;
import dev.chojo.aether.serialization.pojo.guild.MemberPOJO;
import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.data.dao.user.PUser;
import dev.chojo.phoenix.data.dao.user.sub.token.UserToken;
import dev.chojo.phoenix.data.repository.UserRepository;
import dev.chojo.phoenix.service.DiscordOAuthService;
import dev.chojo.phoenix.web.config.GuildRole;
import dev.chojo.phoenix.web.config.UserRole;
import dev.chojo.phoenix.web.service.context.GuildContext;
import dev.chojo.phoenix.web.service.context.UserContext;
import dev.chojo.phoenix.web.service.session.GuildSession;
import io.javalin.http.Context;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final Configuration configuration;
    private final UserRepository userRepository;
    private final DiscordOAuthService discordOAuthService;
    private final ShardManager shardManager;
    // A map that maps a token to the current user session.
    private final Cache<String, UserContext> userSessions;
    private final Cache<Object, GuildSession> guildSessions;

    @Inject
    public SessionService(
            Configuration configuration,
            UserRepository userRepository,
            DiscordOAuthService discordOAuthService,
            ShardManager shardManager) {
        this.configuration = configuration;
        this.userRepository = userRepository;
        this.discordOAuthService = discordOAuthService;
        this.shardManager = shardManager;
        userSessions = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
        guildSessions = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
    }

    public Optional<UserContext> getUserSession(Context ctx) {
        String authorization = ctx.header("Authorization");
        if (authorization == null) return Optional.empty();
        UserContext session = userSessions.getIfPresent(authorization);
        if (session != null) {
            return Optional.of(session);
        }
        return tryReconstructSession(authorization);
    }

    public void logout(String token) {
        if (token.isBlank()) return;
        userRepository.byToken(token).ifPresent(user -> user.tokens().removeToken(token));
        userSessions.invalidate(token);
        // TODO: discordOAuthService.client().invalidate(token);
    }

    public GuildSession getGuildSession(long guildId) {
        try {
            return guildSessions.get(guildId, GuildSession::new);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<UserContext> tryReconstructSession(String token) {
        return userRepository.byToken(token).map(user -> {
            UserToken userToken = user.tokens().token(token).get();
            UserContext session = createUserSessionFromToken(user.id(), token, userToken.lastUsed());
            userSessions.put(token, session);
            userToken.used();
            return session;
        });
    }

    public ShardManager shardManager() {
        return shardManager;
    }

    private UserContext createUserSessionFromToken(long userId, String token, Instant created) {
        Map<String, GuildContext> guilds = new HashMap<>();

        // Build member info for the current user (global, not guild-scoped)
        MemberPOJO memberPojo;
        try {
            User user = shardManager.retrieveUserById(userId).complete();
            if (user != null) {
                memberPojo =
                        new MemberPOJO(user.getName(), String.valueOf(userId), "#ffffff", user.getEffectiveAvatarUrl());
            } else {
                memberPojo = MemberPOJO.generate(String.valueOf(userId));
            }
        } catch (Exception ex) {
            memberPojo = MemberPOJO.generate(String.valueOf(userId));
        }

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(UserRole.ANYONE);

        PUser user = userRepository.byId(userId);
        var userToken = user.tokens().token(token);

        UserContext userContext = new UserContext(userId, token, guilds, memberPojo, created, userRoles);

        if (userToken.isEmpty()) {
            log.warn("No token found for user {}", userId);
            return userContext;
        }

        userRoles.add(UserRole.USER);

        if (configuration.main().general().isOwner(userId)) {
            userRoles.add(UserRole.OWNER);
        }

        List<DiscordGuild> userGuilds;
        try {
            userGuilds = discordOAuthService.client().guilds(userToken.get());
        } catch (Exception e) {
            log.error("Failed to fetch user guilds for user {}", userId, e);
            return userContext;
        }

        for (DiscordGuild discordGuild : userGuilds) {
            String guildId = discordGuild.id();
            Guild guild = shardManager.getGuildById(guildId);
            if (guild == null) continue;

            Set<GuildRole> guildRoles = new HashSet<>();
            Member member;
            try {
                member = guild.retrieveMemberById(userId).complete();
            } catch (Exception e) {
                log.error("Could not build user session for user {} on guild {}", userId, guild, e);
                continue;
            }

            // TODO: Allow to give users admin permissions on specific guilds

            guildRoles.add(GuildRole.MEMBER);
            if (userRoles.contains(UserRole.OWNER) || member.hasPermission(Permission.ADMINISTRATOR)) {
                guildRoles.add(GuildRole.ADMIN);
            }

            guilds.put(guildId, new GuildContext(guildRoles, discordGuild));
        }

        return userContext;
    }
}
