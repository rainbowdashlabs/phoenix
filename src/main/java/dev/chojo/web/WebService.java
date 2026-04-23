/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;
import dev.chojo.aether.commonweb.error.ApiException;
import dev.chojo.aether.commonweb.error.ErrorResponseWrapper;
import dev.chojo.aether.serialization.jackson.DiscordSerializationModule;
import dev.chojo.configuration.Configuration;
import dev.chojo.service.DiscordOAuthService;
import dev.chojo.web.api.Api;
import dev.chojo.web.config.Jackson3Mapper;
import dev.chojo.web.service.SessionService;
import dev.chojo.web.service.context.UserContext;
import io.javalin.Javalin;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.security.RouteRole;
import org.slf4j.Logger;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.VisibilityChecker;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Set;

import static io.javalin.http.staticfiles.Location.CLASSPATH;
import static io.javalin.http.staticfiles.Location.EXTERNAL;
import static org.slf4j.LoggerFactory.getLogger;

public class WebService {
    private static final Logger log = getLogger(WebService.class);
    private final Configuration configuration;
    private Javalin javalin;
    private final SessionService sessionService;
    private final DiscordOAuthService discordOAuthService;

    @Inject
    public WebService(
            Configuration configuration, SessionService sessionService, DiscordOAuthService discordOAuthService) {
        this.configuration = configuration;
        this.sessionService = sessionService;
        this.discordOAuthService = discordOAuthService;
        initApi();
    }

    public static io.javalin.json.JsonMapper jacksonMapper() {
        SimpleModule longAsStringModule = new tools.jackson.databind.module.SimpleModule();
        longAsStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longAsStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(longAsStringModule)
                .addModule(new DiscordSerializationModule())
                .changeDefaultVisibility(WebService::visibilityChecker)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"))
                .build();
        return new Jackson3Mapper(mapper, true);
    }

    private static VisibilityChecker visibilityChecker(VisibilityChecker visibilityChecker) {
        return visibilityChecker
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE);
    }

    private void initApi() {
        var api = configuration.main().api();

        /*
        var options = new OpenApiOptions(info)
                .path("/json-docs")
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .swagger(new SwaggerOptions("/docs").title("Reputation Bot API"));
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);
        */

        javalin = Javalin.create(config -> {
                    config.registerPlugin(new OpenApiPlugin(this::configureOpenApi));
                    config.registerPlugin(new SwaggerPlugin(this::configureSwagger));
                    config.bundledPlugins.enableCors(cors -> {
                        cors.addRule(CorsPluginConfig.CorsRule::anyHost);
                    });
                    // Serve static files from external "public" directory when available; fall back to classpath
                    var publicDir = Path.of("public");
                    if (Files.isDirectory(publicDir)) {
                        config.staticFiles.add(publicDir.toString(), EXTERNAL);
                    } else {
                        config.staticFiles.add("/static", CLASSPATH);
                    }

                    setupExceptionHandler(config.routes);

                    config.routes.apiBuilder(() -> new Api(sessionService, configuration, discordOAuthService).init());
                    config.routes.beforeMatched(this::handleAccess);
                    config.jsonMapper(jacksonMapper());
                    // Serve frontend SPA
                    if (Files.isDirectory(publicDir)) {
                        config.spaRoot.addFile(
                                "/", publicDir.resolve("index.html").toString(), EXTERNAL);
                    } else {
                        config.spaRoot.addFile("/", "/static/index.html", CLASSPATH);
                    }
                })
                .start(api.host(), api.port());
    }

    private void handleAccess(Context ctx) {
        String path = ctx.path();
        if (path.startsWith("/v1/auth/")
                || path.startsWith("/openapi")
                || path.startsWith("/swagger")
                || path.startsWith("/docs")
                || path.startsWith("/json-docs")) {
            return;
        }

        Set<RouteRole> routeRoles = ctx.routeRoles();
    }

    private void addGuildSession(Context ctx, UserContext userContext) {
        ctx.sessionAttribute("X-Guild-Id");
    }

    private void configureSwagger(SwaggerConfiguration swaggerConfiguration) {
        swaggerConfiguration.withDocumentationPath("/docs").withUiPath("/swagger-ui");
    }

    private void setupExceptionHandler(RoutesConfig routes) {
        // Handle specific PremiumFeatureException with detailed JSON
        //        routes.exception(PremiumFeatureException.class, (err, ctx) -> {
        //            var response = new ErrorResponseWrapper("Supporter Required", err.getMessage(), err.details());
        //            ctx.json(response).status(err.status());
        //        });

        routes.exception(JsonMappingException.class, (err, ctx) -> {
            log.error("Invalid JSON on route {}", ctx.path(), err);
            if (err.getCause() instanceof InputCoercionException input) {
                ctx.json(new ErrorResponseWrapper(
                                "Invalid Input",
                                input.getMessage().lines().findFirst().get()))
                        .status(HttpStatus.BAD_REQUEST);
                return;
            }
            ctx.json(new ErrorResponseWrapper(
                            "Invalid Input",
                            err.getMessage().lines().findFirst().get()))
                    .status(HttpStatus.BAD_REQUEST);
        });

        routes.exception(InputCoercionException.class, (err, ctx) -> {
            ctx.json(new ErrorResponseWrapper(
                            "Invalid Input: %s (%s)".formatted(err.getInputType(), err.getMessage()), err.getMessage()))
                    .status(HttpStatus.BAD_REQUEST);
        });

        // Handle generic ApiException with simple JSON
        routes.exception(ApiException.class, (err, ctx) -> {
            var response = new ErrorResponseWrapper(err.getClass().getSimpleName(), err.getMessage());
            ctx.json(response).status(err.status());
        });

        routes.exception(Exception.class, (err, ctx) -> {
            log.error("Unhandled exception on route {}", ctx.path(), err);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    private void configureOpenApi(OpenApiPluginConfiguration config) {
        config.withDocumentationPath("/docs").withDefinitionConfiguration((version, definition) -> {
            definition.info(info -> {
                info.title("Phoenix API");
                info.version("1.0");
                info.description("Documentation for the Phoenix API");
                info.license(
                        "GNU Affero General Public License v3.0",
                        "https://github.com/RainbowDashLabs/phoenix/blob/master/LICENSE.md");
            });
            definition.server(openApiServer -> {
                openApiServer.url("https://phoenix.chojo.dev");
                openApiServer.description("Main server");
            });
        });
    }
}
