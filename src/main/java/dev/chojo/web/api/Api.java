/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.api;

import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.service.DiscordOAuthService;
import dev.chojo.web.api.routes.V1;
import dev.chojo.web.service.SessionService;
import io.javalin.http.ContentType;
import io.javalin.http.HandlerType;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.after;
import static io.javalin.apibuilder.ApiBuilder.before;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final SessionService sessionService;
    private final Configuration configuration;
    private final DiscordOAuthService discordOAuthService;

    @Inject
    public Api(SessionService sessionService, Configuration configuration, DiscordOAuthService discordOAuthService) {
        this.sessionService = sessionService;
        this.configuration = configuration;
        this.discordOAuthService = discordOAuthService;
    }

    public void init() {
        before(ctx -> {
            if (ctx.method() == HandlerType.OPTIONS) return;
            log.trace(
                    "Received request on route: {} {}\nHeaders:\n{}\nBody:\n{}",
                    ctx.method() + " " + ctx.url(),
                    Objects.requireNonNullElse(ctx.queryString(), ""),
                    ctx.headerMap().entrySet().stream()
                            .map(h -> "   " + h.getKey() + ": " + h.getValue())
                            .collect(Collectors.joining("\n")),
                    ctx.body().substring(0, Math.min(ctx.body().length(), 180)));
        });
        after(ctx -> {
            if (ctx.method() == HandlerType.OPTIONS) return;
            log.trace(
                    "Answered request on route: {} {}\nStatus: {}\nHeaders:\n{}\nBody:\n{}",
                    ctx.method() + " " + ctx.url(),
                    Objects.requireNonNullElse(ctx.queryString(), ""),
                    ctx.status(),
                    ctx.res().getHeaderNames().stream()
                            .map(h -> "   " + h + ": " + ctx.res().getHeader(h))
                            .collect(Collectors.joining("\n")),
                    ContentType.OCTET_STREAM.equals(ctx.contentType())
                            ? "Bytes"
                            : Objects.requireNonNullElse(ctx.result(), "")
                                    .substring(
                                            0,
                                            Math.min(
                                                    Objects.requireNonNullElse(ctx.result(), "")
                                                            .length(),
                                                    180)));
        });
        new V1(sessionService, configuration, discordOAuthService).buildRoutes();
    }
}
