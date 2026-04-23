/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.service;

import dev.chojo.aether.discordoauth.access.OAuthToken;
import dev.chojo.aether.discordoauth.configuration.DiscordOAuth;
import dev.chojo.aether.discordoauth.pojo.DiscordUser;
import dev.chojo.aether.discordoauth.pojo.TokenResponse;
import dev.chojo.aether.discordoauth.service.ADiscordOAuthService;
import dev.chojo.data.repository.UserRepository;

import java.time.Instant;
import java.util.List;

public class DiscordOAuthService extends ADiscordOAuthService {

    private final UserRepository userRepository;

    public DiscordOAuthService(DiscordOAuth config, String host, UserRepository userRepository) {
        super(config, host);
        this.userRepository = userRepository;
    }

    @Override
    protected List<? extends OAuthToken> getExpiringtokens(Instant instant) {
        return List.of();
    }

    @Override
    public String userToken(long userId) {
        return userRepository.byId(userId).tokens().;
    }

    @Override
    public void updateUser(DiscordUser discordUser, TokenResponse tokenResponse) {}
}
