/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.service;

import dev.chojo.aether.common.provider.IUserProvider;
import dev.chojo.aether.kofi.configuration.Kofi;
import dev.chojo.aether.kofi.pojo.Type;
import dev.chojo.aether.kofi.service.AKofiService;
import dev.chojo.aether.mailing.service.AMailService;
import dev.chojo.aether.supporter.access.Subscriptions;
import dev.chojo.aether.supporter.configuration.SupporterConfiguration;
import dev.chojo.data.dao.user.sub.purchases.KofiPurchase;

import java.time.Instant;
import java.util.List;

public class KofiService extends AKofiService<KofiPurchase> {
    public KofiService(
            Kofi configuration,
            IUserProvider IUserProvider,
            AMailService mailService,
            SupporterConfiguration<?, ?, ?> supporterConfiguration) {
        super(configuration, IUserProvider, mailService, supporterConfiguration);
    }

    @Override
    protected void registerPurchase(KofiPurchase purchase) {}

    @Override
    protected List<KofiPurchase> expiredPurchases() {
        return List.of();
    }

    @Override
    protected Subscriptions guildSubscriptions(long guildId) {
        return null;
    }

    @Override
    protected KofiPurchase buildPurchase(
            String mailHash, String transactionId, String key, Type type, long subscriptionId, Instant expiresAt) {
        return new KofiPurchase(mailHash, transactionId, key, type, subscriptionId, expiresAt);
    }
}
