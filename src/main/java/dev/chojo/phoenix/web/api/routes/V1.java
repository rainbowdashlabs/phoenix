/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.api.routes;

import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.service.DiscordOAuthService;
import dev.chojo.phoenix.web.api.RoutesBuilder;
import dev.chojo.phoenix.web.api.routes.v1.Auth;
import dev.chojo.phoenix.web.api.routes.v1.Data;
import dev.chojo.phoenix.web.api.routes.v1.Session;
import dev.chojo.phoenix.web.service.SessionService;
import net.dv8tion.jda.api.sharding.ShardManager;

import static io.javalin.apibuilder.ApiBuilder.path;

public class V1 implements RoutesBuilder {
    private final SessionService sessionService;
    private final Configuration configuration;
    private final DiscordOAuthService discordOAuthService;
    private final ShardManager shardManager;

    public V1(
            SessionService sessionService,
            Configuration configuration,
            DiscordOAuthService discordOAuthService,
            ShardManager shardManager) {
        this.sessionService = sessionService;
        this.configuration = configuration;
        this.discordOAuthService = discordOAuthService;
        this.shardManager = shardManager;
    }

    @Override
    public void buildRoutes() {
        path("v1", () -> {
            new Auth(discordOAuthService, sessionService).buildRoutes();
            new Session(sessionService).buildRoutes();
            new Data(shardManager, configuration).buildRoutes();
        });
    }
}
