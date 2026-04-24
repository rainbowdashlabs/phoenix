/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.api.routes.v1.auth;

import dev.chojo.phoenix.service.DiscordOAuthService;
import dev.chojo.phoenix.web.api.RoutesBuilder;
import dev.chojo.phoenix.web.config.UserRole;
import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Login implements RoutesBuilder {
    private final DiscordOAuthService discordOAuthService;

    public Login(DiscordOAuthService discordOAuthService) {
        this.discordOAuthService = discordOAuthService;
    }

    @Override
    public void buildRoutes() {
        get("login", this::login, UserRole.ANYONE);
    }

    @OpenApi(
            path = "/v1/auth/login",
            methods = io.javalin.openapi.HttpMethod.GET,
            summary = "Redirect to Discord OAuth login",
            tags = {"Auth"},
            responses = {@OpenApiResponse(status = "302", description = "Redirect to Discord OAuth")})
    private void login(Context ctx) {
        discordOAuthService.startDiscordLogin(ctx);
    }
}
