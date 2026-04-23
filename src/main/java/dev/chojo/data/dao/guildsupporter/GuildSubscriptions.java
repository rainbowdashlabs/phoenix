/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao.guildsupporter;

import dev.chojo.aether.supporter.access.ISubscriptions;
import dev.chojo.aether.supporter.access.SkuTarget;
import dev.chojo.aether.supporter.access.Subscription;
import dev.chojo.aether.supporter.configuration.modules.subscriptions.platform.Platform;
import dev.chojo.aether.supporter.registry.SupporterRegistry;
import net.dv8tion.jda.api.entities.Entitlement.EntitlementType;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class GuildSubscriptions implements ISubscriptions {
    private final long guildId;

    @Nullable
    private List<Subscription> subscriptions = null;

    public GuildSubscriptions(long guildId) {
        this.guildId = guildId;
    }

    @Override
    public List<Subscription> subscriptions() {
        if (subscriptions != null) return subscriptions;
        subscriptions = query("""
                SELECT
                    target_id,
                    subscription_id,
                    source,
                    target,
                    purchase_type,
                    ends_at,
                    persistent
                FROM
                    subscriptions
                WHERE target_id = ?
                  AND target = 'GUILD'
                """)
                .single(call().bind(guildId))
                .map(row -> new Subscription(
                        row.getLong("subscription_id"),
                        row.getLong("target_id"),
                        SupporterRegistry.PLATFORMS
                                .byName(row.getString("platform"))
                                .orElseThrow(),
                        row.getEnum("target", SkuTarget.class),
                        row.getEnum("purchase_type", EntitlementType.class),
                        row.get("ends_at", INSTANT_TIMESTAMP),
                        row.getBoolean("persistent")))
                .all();
        return subscriptions();
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        query("""
                DELETE FROM subscriptions WHERE subscription_id = ? AND target_id = ?;
                """).single(call().bind(subscription.subscriptionId()).bind(subscription.targetId()));
    }

    @Override
    public boolean addSubscription(Subscription subscription) {
        query("""
                INSERT
                INTO
                    subscriptions
                    (subscription_id, target_id, source, target, purchase_type, ends_at, persistent)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(target_id, target, subscription_id)
                    DO UPDATE
                    SET
                        ends_at    = excluded.ends_at,
                        persistent = excluded.persistent;
                """)
                .single(call().bind(subscription.subscriptionId())
                        .bind(subscription.targetId())
                        .bind(subscription.source().name())
                        .bind(subscription.target())
                        .bind(subscription.purchaseType())
                        .bind(subscription.endsAt(), INSTANT_TIMESTAMP)
                        .bind(subscription.isPersistent()))
                .insert()
                .ifChanged(i -> subscriptions().add(subscription));
        return false;
    }

    @Override
    public void clear(Platform source) {
        query("""
                DELETE FROM subscriptions WHERE target_id = ? AND source = ?;
                """).single(call().bind(guildId).bind(source.name())).delete();
    }
}
