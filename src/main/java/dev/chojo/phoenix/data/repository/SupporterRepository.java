/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import dev.chojo.aether.kofi.pojo.Type;
import dev.chojo.phoenix.data.dao.GuildSupporter;
import dev.chojo.phoenix.data.dao.user.sub.purchases.DiscordPurchase;
import dev.chojo.phoenix.data.dao.user.sub.purchases.KofiPurchase;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class SupporterRepository {
    private final Cache<Long, GuildSupporter> guildSupporters =
            CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

    @Inject
    public SupporterRepository() {}

    public GuildSupporter guild(long guild) {
        try {
            return guildSupporters.get(guild, () -> new GuildSupporter(guild));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerPurchase(KofiPurchase purchase) {
        if (purchase.type() == Type.SUBSCRIPTION) {
            // Renew subscription if one exists already with that mail hash
            Optional<KofiPurchase> matchingPurchase = getMatchingPurchase(purchase);
            if (matchingPurchase.isPresent()) {
                matchingPurchase.get().renew();
                return;
            }
        }
        query("""
                INSERT
                INTO
                    kofi_purchase(mail_hash, key, subscription_id, type, expires_at, transaction_id, guild_id)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(transaction_id)
                    DO NOTHING;
                """)
                .single(call().bind(purchase.mailHash())
                        .bind(purchase.key())
                        .bind(purchase.subscriptionId())
                        .bind(purchase.type())
                        .bind(purchase.expiresAt(), INSTANT_TIMESTAMP)
                        .bind(purchase.transactionId())
                        .bind(purchase.guildId()))
                .insert();
    }

    public Optional<KofiPurchase> getMatchingPurchase(KofiPurchase purchase) {
        return query("""
                SELECT *
                FROM
                    kofi_purchase
                WHERE subscription_id = ?
                  AND mail_hash = ?
                  AND type = ?;
                """)
                .single(call().bind(purchase.subscriptionId())
                        .bind(purchase.mailHash())
                        .bind(purchase.type()))
                .mapAs(KofiPurchase.class)
                .first();
    }

    public List<KofiPurchase> getExpiredKofiPurchased() {
        return query("""
                SELECT
                    id,
                    mail_hash,
                    key,
                    subscription_id,
                    type,
                    expires_at,
                    transaction_id,
                    guild_id
                FROM
                    kofi_purchase
                WHERE expires_at < now()
                  AND type = 'SUBSCRIPTION';
                """).single().mapAs(KofiPurchase.class).all();
    }

    public DiscordPurchase registerPurchase(DiscordPurchase discordPurchase) {
        return query("""
                INSERT
                INTO
                    discord_purchase(user_id, sku_id, type, target, subscription_id, entitlement_id, expires_at, persistent, guild_id)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(entitlement_id) DO NOTHING
                RETURNING user_id, sku_id, type, target, subscription_id, entitlement_id, expires_at, persistent, guild_id
                """)
                .single(call().bind(discordPurchase.userId())
                        .bind(discordPurchase.skuId())
                        .bind(discordPurchase.type())
                        .bind(discordPurchase.target())
                        .bind(discordPurchase.subscriptionId())
                        .bind(discordPurchase.entitlementId())
                        .bind(discordPurchase.expiresAt(), INSTANT_TIMESTAMP)
                        .bind(discordPurchase.isPersistent())
                        .bind(discordPurchase.guildId()))
                .map(DiscordPurchase::new)
                .first()
                .orElseThrow();
    }

    public Optional<DiscordPurchase> getMatchingPurchase(DiscordPurchase discordPurchase) {
        return query("""
                SELECT user_id, sku_id, type, target, subscription_id, entitlement_id, expires_at, persistent, guild_id
                FROM discord_purchase
                WHERE entitlement_id = ?
                """)
                .single(call().bind(discordPurchase.entitlementId()))
                .map(DiscordPurchase::new)
                .first();
    }
}
