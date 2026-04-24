/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.dao.user.sub.purchases;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import dev.chojo.aether.kofi.pojo.AKofiPurchase;
import dev.chojo.aether.kofi.pojo.Type;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class KofiPurchase extends AKofiPurchase {
    private final long id;

    @MappingProvider({"id", "mail_hash", "key", "sku_id", "type", "expires_at", "transaction_id", "guild_id"})
    public KofiPurchase(Row row) throws SQLException {
        this(
                row.getLong("id"),
                row.getString("mail_hash"),
                row.getString("transaction_id"),
                row.getString("key"),
                row.getEnum("type", Type.class),
                row.getLong("subscription_id"),
                row.get("expires_at", INSTANT_TIMESTAMP),
                row.getLong("guild_id"));
    }

    public KofiPurchase(
            String mailHash,
            String transactionId,
            String key,
            Type type,
            long subscriptionId,
            @Nullable Instant expiresAt) {
        this(-1, mailHash, transactionId, key, type, subscriptionId, expiresAt, 0);
    }

    public KofiPurchase(
            long id,
            String mailHash,
            String transactionId,
            String key,
            Type type,
            long subscriptionId,
            @Nullable Instant expiresAt,
            long guildId) {
        super(mailHash, transactionId, key, type, subscriptionId, expiresAt, guildId);
        this.id = id;
    }

    public long id() {
        return id;
    }

    @Override
    public boolean assignToGuild(long guildId) {
        query("""
                UPDATE kofi_purchase SET guild_id = ? WHERE id = ?;
                """).single(call().bind(guildId).bind(id)).update();
        return true;
    }

    @Override
    public boolean unassignFromGuild() {
        query("""
                UPDATE kofi_purchase SET guild_id = 0 WHERE id = ?;
                """).single(call().bind(id)).update();
        return true;
    }

    /**
     * Renew the subscription.
     */
    @Override
    public void renew() {
        query("""
                UPDATE kofi_purchase SET expires_at = now() + '32 days'::INTERVAL WHERE id = ?
                """).single(call().bind(id)).update();
    }

    @Override
    public void delete() {
        query("""
                DELETE FROM kofi_purchase WHERE id = ?;
                """).single(call().bind(id)).delete();
    }
}
