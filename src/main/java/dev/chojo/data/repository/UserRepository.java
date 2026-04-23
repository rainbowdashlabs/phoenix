/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import dev.chojo.aether.kofi.pojo.Type;
import dev.chojo.data.dao.user.PUser;
import dev.chojo.data.dao.user.sub.UserToken;
import dev.chojo.data.dao.user.sub.purchases.KofiPurchase;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class UserRepository {
    private final Cache<Long, PUser> users =
            CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).build();

    public PUser byId(long id) {
        try {
            return users.get(id, () -> new PUser(id));
        } catch (ExecutionException e) {
            return new PUser(id);
        }
    }

    public PUser byUser(User user) {
        return byId(user.getIdLong());
    }

    public Optional<PUser> byMailHash(String hash) {
        return query("""
                SELECT
                    user_id
                FROM
                    user_mails um
                WHERE mail_hash = ?;
                """).single(call().bind(hash)).mapAs(Long.class).first().map(this::byId);
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
                        .bind(purchase.expiresAt(), StandardValueConverter.INSTANT_TIMESTAMP)
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
                .single(call().bind(purchase.subscriptionId()).bind(purchase.mailHash()).bind(purchase.type()))
                .mapAs(KofiPurchase.class)
                .first();
    }

    public void cleanupExpiredMails() {
        query("""
                DELETE
                FROM
                    user_mails
                WHERE verification_requested < now() - INTERVAL '1 hour'
                  AND NOT verified;
                """).single().delete();
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

    public List<UserToken> getExpiringTokens(Instant cutoff) {
        return query("""
                SELECT
                    user_id,
                    access_token,
                    refresh_token,
                    expiry
                FROM
                    user_token
                WHERE expiry < ?;
                """)
                .single(call().bind(cutoff, StandardValueConverter.INSTANT_TIMESTAMP))
                .mapAs(UserToken.class)
                .all();
    }
}
