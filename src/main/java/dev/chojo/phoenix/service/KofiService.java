/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.service;

import com.google.inject.Inject;
import dev.chojo.aether.common.provider.IUserProvider;
import dev.chojo.aether.kofi.configuration.Kofi;
import dev.chojo.aether.kofi.pojo.Type;
import dev.chojo.aether.kofi.service.AKofiService;
import dev.chojo.aether.mailing.service.AMailService;
import dev.chojo.aether.supporter.access.ISubscriptions;
import dev.chojo.aether.supporter.configuration.SupporterConfiguration;
import dev.chojo.phoenix.data.dao.user.sub.purchases.KofiPurchase;
import dev.chojo.phoenix.data.repository.SupporterRepository;

import java.time.Instant;
import java.util.List;

public class KofiService extends AKofiService<KofiPurchase> {
    private final SupporterRepository supporterRepository;

    @Inject
    public KofiService(
            Kofi configuration,
            IUserProvider IUserProvider,
            AMailService mailService,
            SupporterConfiguration<?, ?, ?> supporterConfiguration,
            SupporterRepository supporterRepository) {
        super(configuration, IUserProvider, mailService, supporterConfiguration);
        this.supporterRepository = supporterRepository;
    }

    @Override
    protected void registerPurchase(KofiPurchase purchase) {
        supporterRepository.registerPurchase(purchase);
    }

    @Override
    protected List<KofiPurchase> expiredPurchases() {
        return supporterRepository.getExpiredKofiPurchased();
    }

    @Override
    protected ISubscriptions guildSubscriptions(long guildId) {
        return supporterRepository.guild(guildId);
    }

    @Override
    protected KofiPurchase buildPurchase(
            String mailHash, String transactionId, String key, Type type, long subscriptionId, Instant expiresAt) {
        return new KofiPurchase(mailHash, transactionId, key, type, subscriptionId, expiresAt);
    }
}
