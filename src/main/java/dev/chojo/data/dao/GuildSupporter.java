/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao;

import dev.chojo.aether.supporter.access.ISubscriptions;
import dev.chojo.aether.supporter.access.SkuTarget;
import dev.chojo.aether.supporter.access.Subscription;
import dev.chojo.aether.supporter.configuration.modules.subscriptions.platform.Platform;
import dev.chojo.data.converter.AetherConverter;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static net.dv8tion.jda.api.entities.Entitlement.EntitlementType;

public class GuildSupporter implements ISubscriptions {
    private final long guildId;

    @Nullable
    private List<Subscription> subscriptions;

    public GuildSupporter(long guildId) {
        this.guildId = guildId;
    }

    @Override
    public List<Subscription> subscriptions() {
        if (subscriptions != null) return subscriptions;
        subscriptions = query("""
                SELECT target_id, subscription_id, source, target, purchase_type, ends_at, persistent FROM subscriptions WHERE target_id = ?;
                """)
                .single(call().bind(guildId))
                .map(e -> new Subscription(
                        e.getLong("subscription_id"),
                        e.getLong("target_id"),
                        e.get("platform", AetherConverter.PLATFORM),
                        e.getEnum("target", SkuTarget.class),
                        e.getEnum("purchase_type", EntitlementType.class),
                        e.get("ends_at", INSTANT_TIMESTAMP),
                        e.getBoolean("persistent")))
                .all();
        return subscriptions();
    }

    @Override
    public void deleteSubscription(Subscription subscription) {
        query("""
                DELETE FROM subscriptions
                WHERE subscription_id = ? AND target_id = ?;
                """)
                .single(call().bind(subscription.subscriptionId()).bind(subscription.targetId()))
                .delete()
                .ifChanged(i -> subscriptions()
                        .removeIf(s -> s.subscriptionId() == subscription.subscriptionId()
                                && s.targetId() == subscription.targetId()));
    }

    @Override
    public boolean addSubscription(Subscription subscription) {
        query("""

                INSERT
            INTO
                subscriptions(target_id, subscription_id, source, target, purchase_type, ends_at, persistent)
            VALUES
                (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (target_id, subscription_id)
                DO UPDATE
                SET
                    ends_at    = excluded.ends_at,
                    persistent = excluded.persistent;
            """)
                .single(call().bind(subscription.targetId())
                        .bind(subscription.subscriptionId())
                        .bind(subscription.source().name())
                        .bind(subscription.target())
                        .bind(subscription.purchaseType().name())
                        .bind(subscription.endsAt(), INSTANT_TIMESTAMP)
                        .bind(subscription.isPersistent()));
        return false;
    }

    @Override
    public void clear(Platform source) {
        query("""
                DELETE FROM subscriptions
                WHERE target_id = ?
                AND source = ?;
                """).single(call().bind(guildId).bind(source.name()));
    }
}
