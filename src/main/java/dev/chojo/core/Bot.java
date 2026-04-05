/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class Bot extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final Configuration configuration;

    @Inject
    public Bot(Configuration configuration) {
        this.configuration = configuration;
    }

    @Inject
    public void start() throws InterruptedException {
        ShardManager manager = shardManager(configuration.main().general().token());
        jdaCommands(manager);

        Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("An uncaught exception has occurred!", e));
        Runtime.getRuntime().addShutdownHook(new Thread(manager::shutdown));

        manager.setPresence(OnlineStatus.ONLINE, Activity.customStatus("Vibing"));
    }

    private ShardManager shardManager(String token) throws InterruptedException {
        ShardManager manager = DefaultShardManagerBuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setEventPool(Executors.newVirtualThreadPerTaskExecutor())
                .build();
        for (JDA shard : manager.getShards()) {
            shard.awaitReady();
        }
        return manager;
    }

    private void jdaCommands(ShardManager manager) {
        JDACommands.builder(manager)
                .packages("dev.chojo")
                .extensionData(new GuiceExtensionData(Guice.createInjector(this)))
                .start();
    }
}
