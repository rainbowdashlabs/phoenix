/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.api.routes.v1;

import dev.chojo.service.DiscordOAuthService;
import dev.chojo.web.api.RoutesBuilder;
import dev.chojo.web.api.routes.v1.auth.Callback;
import dev.chojo.web.api.routes.v1.auth.Login;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Auth implements RoutesBuilder {
    private final DiscordOAuthService discordOAuthService;

    public Auth(DiscordOAuthService discordOAuthService) {
        this.discordOAuthService = discordOAuthService;
    }

    @Override
    public void buildRoutes() {
        path("auth", () -> {
            new Login(discordOAuthService).buildRoutes();
            new Callback(discordOAuthService).buildRoutes();
        });
    }
}
