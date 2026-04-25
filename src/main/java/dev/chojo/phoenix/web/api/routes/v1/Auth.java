/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.api.routes.v1;

import dev.chojo.phoenix.service.DiscordOAuthService;
import dev.chojo.phoenix.web.api.RoutesBuilder;
import dev.chojo.phoenix.web.config.UserRole;
import dev.chojo.phoenix.web.service.SessionService;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Auth implements RoutesBuilder {
    private final DiscordOAuthService discordOAuthService;
    private final SessionService sessionService;

    public Auth(DiscordOAuthService discordOAuthService, SessionService sessionService) {
        this.discordOAuthService = discordOAuthService;
        this.sessionService = sessionService;
    }

    @Override
    public void buildRoutes() {
        path("auth", () -> {
            get("login", this::login, UserRole.ANYONE);
            get("callback", this::callback, UserRole.ANYONE);
            post("logout", this::logout, UserRole.USER);
        });
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

    @OpenApi(
            path = "/v1/auth/logout",
            methods = io.javalin.openapi.HttpMethod.POST,
            summary = "Logout current user session",
            tags = {"Auth"},
            responses = {
                @OpenApiResponse(status = "204", description = "Successfully logged out"),
                @OpenApiResponse(status = "401", description = "Not logged in")
            })
    private void logout(Context ctx) {
        String token = ctx.header("Authorization");
        if (token == null) throw new UnauthorizedResponse("Not logged in");
        sessionService.logout(token);
        ctx.status(204);
    }
}
