/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.core;

import com.google.inject.Guice;
import com.google.inject.Provides;
import dev.chojo.configuration.Configuration;
import dev.chojo.crypto.CryptoService;
import dev.chojo.data.SaduModule;
import io.github.kaktushose.jdac.JDACBuilder;
import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.annotations.interactions.CommandScope;
import io.github.kaktushose.jdac.definitions.interactions.command.CommandDefinition;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Bot extends SaduModule {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final Configuration configuration;
    private final CryptoService cryptoService;

    public Bot(Configuration configuration) throws SQLException, IOException, NoSuchAlgorithmException {
        super(configuration);
        this.configuration = configuration;
        cryptoService = new CryptoService(configuration);
    }

    public void start() throws InterruptedException {
        ShardManager manager = shardManager(configuration.main().general().token());
        jdaCommands(manager);

        Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("An uncaught exception has occurred!", e));
        Runtime.getRuntime().addShutdownHook(new Thread(manager::shutdown));

        manager.setPresence(OnlineStatus.ONLINE, Activity.customStatus("Vibing"));
    }

    @Provides
    public Configuration configuration() {
        return configuration;
    }

    @Provides
    public CryptoService cryptoService() {
        return cryptoService;
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
