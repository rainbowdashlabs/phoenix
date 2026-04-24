/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.service;

import com.google.inject.Inject;
import dev.chojo.aether.discordoauth.pojo.DiscordGuild;
import dev.chojo.configuration.Configuration;
import dev.chojo.web.config.GuildRole;
import dev.chojo.web.config.SessionAttribute;
import dev.chojo.web.config.UserRole;
import dev.chojo.web.service.context.GuildContext;
import dev.chojo.web.service.context.UserContext;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Set;

public class SecurityService {
    private final SessionService sessionService;
    private final Configuration configuration;

    @Inject
    public SecurityService(SessionService sessionService, Configuration configuration) {
        this.sessionService = sessionService;
        this.configuration = configuration;
    }

    public void check(Context ctx) {
        // Exempt auth and static/OpenAPI routes from authorization checks
        String path = ctx.path();
        if (path.startsWith("/v1/auth/")
                || path.startsWith("/openapi")
                || path.startsWith("/swagger")
                || path.startsWith("/docs")
                || path.startsWith("/json-docs")) {
            return;
        }

        Set<RouteRole> routeRoles = ctx.routeRoles();
        if (routeRoles.contains(UserRole.ANYONE) || routeRoles.isEmpty()) {
            return;
        }

        UserContext userContext = sessionService.getUserSession(ctx).orElseThrow(() -> {
            ctx.header("WWW-Authenticate", "Authorization");
            return new UnauthorizedResponse("You need to be logged in to access this route.");
        });

        ctx.sessionAttribute(SessionAttribute.USER_SESSION, userContext);

        String guildIdStr = ctx.header("X-Guild-Id");
        if (guildIdStr != null) {
            ctx.sessionAttribute(SessionAttribute.GUILD_ID, guildIdStr);
            var guildData = userContext.guilds().get(guildIdStr);
            if (guildData == null && userContext.hasAccess(Set.of(UserRole.OWNER))) {
                // Create a fake guild
                guildData =
                        new GuildContext(Set.of(GuildRole.ADMIN), new DiscordGuild(guildIdStr, null, "", "", "", true));
            }

            if (guildData != null) {
                if (!guildData.hasAccess(routeRoles)) {
                    throw new UnauthorizedResponse("Permission denied. Required roles: " + routeRoles);
                }

                long guildId = Long.parseLong(guildIdStr);

                if (userContext.hasAccess(Set.of(UserRole.OWNER))
                        && guildData.guild().name() == null) {
                    // Ensure the guild actually exists and the bot is in it if the owner wants to access it
                    if (sessionService.shardManager().getGuildById(guildId) == null) {
                        throw new UnauthorizedResponse(
                                "The requested guild does not exist or the bot is not a member.");
                    }
                }

                ctx.sessionAttribute(SessionAttribute.GUILD_SESSION, sessionService.getGuildSession(guildId));
            } else {
                throw new UnauthorizedResponse("You are not a member of the requested guild.");
            }
        } else if (routeRoles.stream().anyMatch(role -> role instanceof GuildRole)) {
            throw new UnauthorizedResponse("Guild context missing. Please provide X-Guild-Id header.");
        }
    }
}
