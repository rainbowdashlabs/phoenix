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
import dev.chojo.service.MessageStore;
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

import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.Executors;

public class Bot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final Configuration configuration;
    private final CryptoService cryptoService;

    public Bot(Configuration configuration) throws NoSuchAlgorithmException {
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
                                                         .addEventListeners(new MessageStore())
                                                         .build();
        for (JDA shard : manager.getShards()) {
            shard.awaitReady();
        }
        return manager;
    }

    private void jdaCommands(ShardManager manager) {
        JDACBuilder builder = JDACommands.builder(manager)
                                         .packages("dev.chojo")
                                         .extensionData(new GuiceExtensionData(Guice.createInjector(this)));

        // @Nora set dev mode here, either from configuration or env, idk what you like
        if (configuration.main().general().testmode()) {
            // workaround dev mode until #309 is implemented.
            // !!! Doesn't work if commands have @CommandConfig annotation
            builder.globalCommandConfig(CommandDefinition.CommandConfig.of(config -> config.scope(CommandScope.GUILD)));
            builder.guildScopeProvider(commandData -> Set.of(configuration.main().general().botguild()));
        }
        builder.start();
    }
}
