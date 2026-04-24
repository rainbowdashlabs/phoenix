/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.dao.user.sub.purchases;

import de.chojo.sadu.mapper.wrapper.Row;
import dev.chojo.aether.supporter.access.ADiscordPurchase;
import dev.chojo.aether.supporter.access.SkuTarget;
import net.dv8tion.jda.api.entities.Entitlement;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class DiscordPurchase extends ADiscordPurchase {
    public DiscordPurchase(
            long userId,
            long skuId,
            Entitlement.EntitlementType type,
            SkuTarget target,
            long subscriptionId,
            long entitlementId,
            @Nullable Instant expiresAt,
            boolean persistent,
            long guildId) {
        super(userId, skuId, type, target, subscriptionId, entitlementId, expiresAt, persistent, guildId);
    }

    public DiscordPurchase(Row row) throws SQLException {
        this(
                row.getLong("user_id"),
                row.getLong("sku_id"),
                row.getEnum("type", Entitlement.EntitlementType.class),
                row.getEnum("target", SkuTarget.class),
                row.getLong("subscription_id"),
                row.getLong("entitlement_id"),
                row.get("expires_at", INSTANT_TIMESTAMP),
                row.getBoolean("persistent"),
                row.getLong("guild_id"));
    }

    @Override
    public void renew(Instant expiresAt) {
        query("""
                UPDATE discord_purchase
                SET
                    expires_at = ?
                WHERE user_id = ?
                  AND entitlement_id = ?;
                """)
                .single(call().bind(expiresAt, INSTANT_TIMESTAMP).bind(userId).bind(entitlementId))
                .update();
    }

    @Override
    public void delete() {
        query("""
                DELETE
                FROM
                    discord_purchase
                WHERE user_id = ?
                  AND entitlement_id = ?;
                """).single(call().bind(userId).bind(entitlementId)).delete();
    }

    @Override
    public boolean unassignFromGuild() {
        return query("""
                UPDATE discord_purchase
                SET
                    guild_id = 0
                WHERE user_id = ?
                  AND entitlement_id = ?;
                """)
                .single(call().bind(userId).bind(entitlementId))
                .update()
                .changed();
    }

    @Override
    public boolean assignToGuild(long guildId) {
        return query("""
                UPDATE discord_purchase
                SET
                    guild_id = ?
                WHERE user_id = ?
                  AND entitlement_id = ?;
                """)
                .single(call().bind(guildId).bind(userId).bind(entitlementId))
                .update()
                .changed();
    }
}
