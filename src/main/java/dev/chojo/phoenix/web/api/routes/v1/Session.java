/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.api.routes.v1;

import dev.chojo.aether.serialization.pojo.guild.MemberPOJO;
import dev.chojo.phoenix.web.api.RoutesBuilder;
import dev.chojo.phoenix.web.config.UserRole;
import dev.chojo.phoenix.web.service.SessionService;
import dev.chojo.phoenix.web.service.context.UserContext;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Session implements RoutesBuilder {
    private final SessionService sessionService;

    public Session(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void buildRoutes() {
        path("session", () -> {
            get("user", this::user, UserRole.USER);
        });
    }

    @OpenApi(
            path = "/v1/session/user",
            methods = io.javalin.openapi.HttpMethod.GET,
            summary = "Get current user session",
            tags = {"Session"},
            responses = {
                @OpenApiResponse(status = "200", content = @io.javalin.openapi.OpenApiContent(from = MemberPOJO.class)),
                @OpenApiResponse(status = "401", description = "Not logged in")
            })
    private void user(Context ctx) {
        UserContext session =
                sessionService.getUserSession(ctx).orElseThrow(() -> new UnauthorizedResponse("Not logged in"));
        ctx.json(session.user());
    }
}
