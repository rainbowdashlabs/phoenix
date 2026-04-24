/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.config;

import io.javalin.security.RouteRole;

public enum UserRole implements RouteRole {
    /**
     * A not authenticated user.
     */
    ANYONE,
    /**
     * A logged-in user.
     */
    USER,
    /**
     * A bot owner.
     */
    OWNER;
}
