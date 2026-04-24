/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.config;

import io.javalin.security.RouteRole;

public enum GuildRole implements RouteRole {
    /**
     * A user unknown to the guild.
     */
    UNKNOWN,
    /**
     * A member of a guild.
     */
    MEMBER,
    /**
     * The user of a guild that the bot is in with the administrator rights, allowing to modify bot settings.
     */
    ADMIN
}
