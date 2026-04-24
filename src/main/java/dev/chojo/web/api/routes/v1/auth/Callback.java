/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.api.routes.v1.auth;

import dev.chojo.service.DiscordOAuthService;
import dev.chojo.web.api.RoutesBuilder;
import dev.chojo.web.config.UserRole;
import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Callback implements RoutesBuilder {
    private final DiscordOAuthService discordOAuthService;

    public Callback(DiscordOAuthService discordOAuthService) {
        this.discordOAuthService = discordOAuthService;
    }

    @Override
    public void buildRoutes() {
        get("callback", this::callback, UserRole.ANYONE);
    }

    @OpenApi(
            path = "/v1/auth/callback",
            methods = io.javalin.openapi.HttpMethod.GET,
            summary = "Handle Discord OAuth callback",
            tags = {"Auth"},
            queryParams = {@OpenApiParam(name = "code", description = "OAuth code from Discord", required = true)},
            responses = {
                @OpenApiResponse(status = "200", description = "Login successful"),
                @OpenApiResponse(status = "401", description = "Login failed")
            })
    private void callback(Context ctx) {
        discordOAuthService.handleDiscordCallback(ctx);
    }
}
