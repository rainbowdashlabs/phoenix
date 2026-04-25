/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.api.routes.v1;

import dev.chojo.aether.serialization.pojo.guild.MemberPOJO;
import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.sub.Links;
import dev.chojo.phoenix.web.api.RoutesBuilder;
import dev.chojo.phoenix.web.api.schema.MemberSchema;
import dev.chojo.phoenix.web.config.UserRole;
import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.sharding.ShardManager;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Data implements RoutesBuilder {
    private final ShardManager shardManager;
    private final Configuration configuration;

    public Data(ShardManager shardManager, Configuration configuration) {
        this.shardManager = shardManager;
        this.configuration = configuration;
    }

    @Override
    public void buildRoutes() {
        path("data", () -> {
            get("application", this::application, UserRole.ANYONE);
            get("links", this::links, UserRole.ANYONE);
        });
    }

    @OpenApi(
            path = "/v1/data/application",
            methods = io.javalin.openapi.HttpMethod.GET,
            summary = "Get bot application info",
            tags = {"Data"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = @io.javalin.openapi.OpenApiContent(from = MemberSchema.class))
            })
    private void application(Context ctx) {
        var shards = shardManager.getShards();
        if (shards.isEmpty()) {
            ctx.status(503);
            return;
        }
        SelfUser selfUser = shards.get(0).getSelfUser();
        ctx.json(new MemberPOJO(
                selfUser.getEffectiveName(), selfUser.getId(), "#ffffff", selfUser.getEffectiveAvatarUrl()));
    }

    @OpenApi(
            path = "/v1/data/links",
            methods = io.javalin.openapi.HttpMethod.GET,
            summary = "Get configured application links",
            tags = {"Data"},
            responses = {
                @OpenApiResponse(status = "200", content = @io.javalin.openapi.OpenApiContent(from = Links.class))
            })
    private void links(Context ctx) {
        var shards = shardManager.getShards();
        if (shards.isEmpty()) {
            ctx.status(503);
            return;
        }
        ctx.json(
                configuration.main().links().resolve(shards.get(0).getSelfUser().getId()));
    }
}
