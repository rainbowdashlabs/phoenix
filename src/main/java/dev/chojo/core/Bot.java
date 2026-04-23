/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.core;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import dev.chojo.aether.supporter.service.SupporterMiddleware;
import dev.chojo.configuration.Configuration;
import dev.goldmensch.fluava.Fluava;
import io.github.kaktushose.jdac.JDACBuilder;
import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.annotations.interactions.CommandScope;
import io.github.kaktushose.jdac.definitions.interactions.command.CommandDefinition;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import io.github.kaktushose.jdac.message.i18n.FluavaLocalizer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class Bot extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private final Configuration configuration;

    @Nullable
    private ShardManager shardManager;

    @Inject
    public Bot(Configuration configuration) {
        this.configuration = configuration;
    }

    @Provides
    public ShardManager shardManager(Configuration configuration) throws InterruptedException {
        if (shardManager == null) {
            shardManager(configuration.main().general().token());
        }
        return shardManager;
    }

    public void start(Injector injector) throws InterruptedException {
        shardManager(configuration.main().general().token());
        jdaCommands(shardManager, injector);

        Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("An uncaught exception has occurred!", e));
        Runtime.getRuntime().addShutdownHook(new Thread(shardManager::shutdown));

        shardManager.setPresence(OnlineStatus.ONLINE, Activity.customStatus("Vibing"));
    }

    private ShardManager shardManager(String token) throws InterruptedException {
        shardManager = DefaultShardManagerBuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setEventPool(Executors.newVirtualThreadPerTaskExecutor())
                .addEventListeners()
                .build();
        for (JDA shard : shardManager.getShards()) {
            shard.awaitReady();
        }
        return shardManager;
    }

    private void jdaCommands(ShardManager manager, Injector injector) {
        Fluava fluava =
                Fluava.builder().fallback(Locale.UK).bundleRoot("locale").build();
        JDACBuilder builder = JDACommands.builder(manager)
                .packages("dev.chojo")
                .extensionData(new GuiceExtensionData(injector))
                .localizer(FluavaLocalizer.create(fluava))
                .middleware(new SupporterMiddleware<>());

        // @Nora set dev mode here, either from configuration or env, idk what you like
        if (configuration.main().general().testmode()) {
            // workaround dev mode until #309 is implemented.
            // !!! Doesn't work if commands have @CommandConfig annotation
            builder.globalCommandConfig(CommandDefinition.CommandConfig.of(config -> config.scope(CommandScope.GUILD)));
            builder.guildScopeProvider(
                    _ -> Set.of(configuration.main().general().botguild()));
        }
        builder.start();
    }
}
