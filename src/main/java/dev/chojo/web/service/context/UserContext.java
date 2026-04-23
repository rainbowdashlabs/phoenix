/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.service.context;

import dev.chojo.web.config.UserRole;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.Map;

public record UserContext(
        long userId, String token, Map<String, GuildContext> guilds, User user, Instant created, UserRole role) {}
