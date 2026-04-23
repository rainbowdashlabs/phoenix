/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dev.chojo.aether.common.provider.IUserProvider;
import dev.chojo.aether.discordoauth.configuration.DiscordOAuth;
import dev.chojo.aether.discordoauth.service.ADiscordOAuthService;
import dev.chojo.aether.kofi.configuration.Kofi;
import dev.chojo.aether.mailing.IUserMails;
import dev.chojo.aether.mailing.service.AMailService;
import dev.chojo.aether.mailing.service.IUserMailsProvider;
import dev.chojo.aether.mailing.service.MailServiceConfig;
import dev.chojo.aether.mailing.service.MailTemplates;
import dev.chojo.aether.supporter.configuration.SupporterConfiguration;
import dev.chojo.configuration.Configuration;
import dev.chojo.core.Bot;
import dev.chojo.data.dao.user.PUser;
import dev.chojo.data.dao.user.sub.mailing.UserMail;
import dev.chojo.data.dao.user.sub.mailing.UserMails;
import dev.chojo.data.repository.UserRepository;
import dev.chojo.service.DiscordOAuthService;
import dev.chojo.service.MailService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ElpisModule extends AbstractModule {

    private final Configuration configuration;

    public ElpisModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(configuration);
        install(new Bot(configuration));

        bind(AMailService.class).to(MailService.class).in(Singleton.class);
        bind(ADiscordOAuthService.class).to(DiscordOAuthService.class).in(Singleton.class);
    }

    @Provides
    @Named("host")
    public String host() {
        return configuration.main().api().url();
    }

    @Provides
    public SupporterConfiguration<?, ?, ?> supporterConfiguration() {
        return configuration.main().supporter();
    }

    @Provides
    public IUserProvider userProvider(ShardManager shardManager) {
        return new IUserProvider() {

            @Override
            public Optional<User> byId(long id) {
                try {
                    return Optional.ofNullable(shardManager.retrieveUserById(id).complete());
                } catch (Exception e) {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<User> byId(String id) {
                try {
                    return Optional.ofNullable(shardManager.retrieveUserById(id).complete());
                } catch (Exception e) {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<Member> byId(long id, long guildId) {
                try {
                    return byId(id, shardManager.getGuildById(guildId));
                } catch (Exception e) {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<Member> byId(long id, @Nullable Guild guild) {
                if (guild == null) return Optional.empty();

                try {
                    return Optional.ofNullable(guild.retrieveMemberById(id).complete());
                } catch (Exception e) {
                    return Optional.empty();
                }
            }
        };
    }

    @Provides
    public MailServiceConfig<UserMail> mailConfig(IUserMailsProvider<UserMail> provider, UserRepository repository) {
        MailTemplates templates = new MailTemplates();
        return new MailServiceConfig<>(
                configuration.main().api().url(),
                configuration.main().mailing(),
                templates,
                provider,
                repository::cleanupExpiredMails);
    }

    @Provides
    public IUserMailsProvider<UserMail> userMailsProvider(UserRepository repository) {
        return new IUserMailsProvider<>() {
            @Override
            public UserMails byUser(long userId) {
                return repository.byId(userId).mails();
            }

            @Override
            public Optional<IUserMails<UserMail>> byHash(String mailHash) {
                return repository.byMailHash(mailHash).map(PUser::mails);
            }
        };
    }

    @Provides
    public DiscordOAuth discordOAuth() {
        return configuration.main().discordOAuth();
    }

    @Provides
    public Kofi kofi() {
        return configuration.main().kofi();
    }
}
