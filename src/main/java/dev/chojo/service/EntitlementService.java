/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.service;

import com.google.inject.Inject;
import dev.chojo.aether.supporter.access.ISubscriptions;
import dev.chojo.aether.supporter.access.SkuTarget;
import dev.chojo.aether.supporter.configuration.SupporterConfiguration;
import dev.chojo.aether.supporter.configuration.modules.subscriptions.platform.purchase.PurchaseType;
import dev.chojo.aether.supporter.service.AEntitlementService;
import dev.chojo.data.dao.user.sub.purchases.DiscordPurchase;
import dev.chojo.data.repository.SupporterRepository;
import net.dv8tion.jda.api.entities.Entitlement;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public class EntitlementService extends AEntitlementService<DiscordPurchase> {
    private final SupporterRepository repository;

    @Inject
    public EntitlementService(SupporterConfiguration<?, ?, ?> configuration, SupporterRepository repository) {
        super(configuration);
        this.repository = repository;
    }

    @Override
    protected DiscordPurchase buildPurchase(
            long userId,
            long skuId,
            Entitlement.EntitlementType type,
            SkuTarget target,
            long subscriptionId,
            long entitlementId,
            @Nullable Instant expiresAt,
            long guildId,
            PurchaseType purchaseType) {
        return new DiscordPurchase(
                userId,
                skuId,
                type,
                target,
                subscriptionId,
                entitlementId,
                expiresAt,
                purchaseType.isLifetime(),
                guildId);
    }

    @Override
    protected DiscordPurchase registerPurchase(DiscordPurchase discordPurchase) {
        return repository.registerPurchase(discordPurchase);
    }

    @Override
    protected DiscordPurchase updatePurchase(DiscordPurchase discordPurchase) {
        DiscordPurchase match = repository.getMatchingPurchase(discordPurchase).orElseThrow();
        match.renew(discordPurchase.expiresAt());
        return match;
    }

    @Override
    protected DiscordPurchase unregisterPurchase(DiscordPurchase discordPurchase) {
        DiscordPurchase purchase =
                repository.getMatchingPurchase(discordPurchase).orElseThrow();
        purchase.delete();
        return purchase;
    }

    @Override
    protected ISubscriptions guildSubscriptions(long guildId) {
        return repository.guild(guildId);
    }

    @Override
    protected List<DiscordPurchase> getExpiredPurchases() {
        return List.of();
    }
}
