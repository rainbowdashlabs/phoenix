/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.service;

import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dev.chojo.aether.discordoauth.access.IOAuthToken;
import dev.chojo.aether.discordoauth.configuration.DiscordOAuth;
import dev.chojo.aether.discordoauth.pojo.DiscordUser;
import dev.chojo.aether.discordoauth.pojo.TokenResponse;
import dev.chojo.aether.discordoauth.service.ADiscordOAuthService;
import dev.chojo.phoenix.data.dao.user.sub.token.UserToken;
import dev.chojo.phoenix.data.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Singleton
public class DiscordOAuthService extends ADiscordOAuthService {

    private final UserRepository userRepository;

    @Inject
    public DiscordOAuthService(DiscordOAuth config, @Named("host") String host, UserRepository userRepository) {
        super(config, host);
        this.userRepository = userRepository;
    }

    @Override
    protected List<? extends IOAuthToken> getExpiringtokens(Instant instant) {
        return userRepository.getExpiringTokens(instant);
    }

    @Override
    public String storeToken(DiscordUser user, TokenResponse response) {
        var randomString = ThreadLocalRandom.current()
                .ints(10, 'a', 'z')
                .limit(25)
                .mapToObj(Character::toString)
                .collect(Collectors.joining());

        randomString = Hashing.sha256()
                .hashBytes("%s%s".formatted(user.id(), randomString).getBytes(StandardCharsets.UTF_8))
                .toString();

        UserToken userToken = new UserToken(
                user.id(),
                response.accessToken(),
                response.refreshToken(),
                response.expiry(),
                response.scopes(),
                randomString,
                Instant.now());
        return userRepository.byId(user.id()).tokens().addToken(userToken).token();
    }
}
