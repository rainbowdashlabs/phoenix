package dev.chojo;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class Elpis extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(Elpis.class);

    public Elpis(String token) throws InterruptedException {
        JDA jda = jda(token);
        jdaCommands(jda);

        Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("An uncaught exception has occurred!", e));
        Runtime.getRuntime().addShutdownHook(new Thread(jda::shutdown));

        jda.getPresence().setPresence(OnlineStatus.ONLINE, false);
    }

    private JDA jda(String token) throws InterruptedException {
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setEventPool(Executors.newVirtualThreadPerTaskExecutor())
                .build()
                .awaitReady();
    }

    private void jdaCommands(JDA jda) {
        JDACommands.builder(jda)
                .packages("dev.chojo")
                .extensionData(new GuiceExtensionData(Guice.createInjector(this)))
                .start();
    }
}
