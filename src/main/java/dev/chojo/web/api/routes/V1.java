/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.api.routes;

import dev.chojo.configuration.Configuration;
import dev.chojo.service.DiscordOAuthService;
import dev.chojo.web.api.RoutesBuilder;
import dev.chojo.web.api.routes.v1.Auth;
import dev.chojo.web.api.routes.v1.Session;
import dev.chojo.web.service.SessionService;

import static io.javalin.apibuilder.ApiBuilder.path;

public class V1 implements RoutesBuilder {
    private final SessionService sessionService;
    private final Configuration configuration;
    private final DiscordOAuthService discordOAuthService;

    public V1(SessionService sessionService, Configuration configuration, DiscordOAuthService discordOAuthService) {
        this.sessionService = sessionService;
        this.configuration = configuration;
        this.discordOAuthService = discordOAuthService;
    }

    @Override
    public void buildRoutes() {
        path("v1", () -> {
            new Auth(discordOAuthService).buildRoutes();
            new Session(sessionService).buildRoutes();
        });
    }
}
