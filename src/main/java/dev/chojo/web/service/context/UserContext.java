/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.service.context;

import dev.chojo.aether.serialization.pojo.guild.MemberPOJO;
import dev.chojo.web.config.UserRole;
import io.javalin.security.RouteRole;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record UserContext(
        long userId,
        String token,
        Map<String, GuildContext> guilds,
        MemberPOJO user,
        Instant created,
        Set<UserRole> role) {

    public boolean hasAccess(Set<RouteRole> required) {
        for (UserRole userRole : role) {
            if (required.contains(userRole)) return true;
        }
        return false;
    }

    public boolean hasAccess(Set<RouteRole> required, String guildId) {
        return Optional.ofNullable(guilds.get(guildId))
                .map(c -> c.hasAccess(required))
                .orElse(false);
    }
}
