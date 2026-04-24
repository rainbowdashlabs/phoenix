/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.service.context;

import dev.chojo.aether.discordoauth.pojo.DiscordGuild;
import dev.chojo.phoenix.web.config.GuildRole;
import io.javalin.security.RouteRole;

import java.util.Set;

public record GuildContext(Set<GuildRole> roles, DiscordGuild guild) {
    public boolean hasAccess(Set<RouteRole> required) {
        for (GuildRole role : roles) {
            if (required.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
