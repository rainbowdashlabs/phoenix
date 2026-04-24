/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.converter;

import de.chojo.sadu.mapper.reader.ValueReader;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.adapter.Adapter;
import de.chojo.sadu.queries.call.adapter.StandardAdapter;
import de.chojo.sadu.queries.converter.ValueConverter;
import dev.chojo.aether.common.registry.Key;
import dev.chojo.aether.common.registry.Registry;
import dev.chojo.aether.discordoauth.access.OAuthScope;
import dev.chojo.aether.mailing.entities.MailSource;
import dev.chojo.aether.mailing.service.MailSourceRegistry;
import dev.chojo.aether.supporter.configuration.modules.subscriptions.SubscriptionKey;
import dev.chojo.aether.supporter.configuration.modules.subscriptions.platform.Platform;
import dev.chojo.aether.supporter.configuration.modules.subscriptions.platform.purchase.PurchaseType;
import dev.chojo.aether.supporter.registry.SupporterRegistry;

import java.sql.Types;
import java.util.Collection;
import java.util.List;

public final class AetherConverter {
    public static final ValueConverter<MailSource, String> MAIL_SOURCE =
            registryConverter(MailSource.class, MailSourceRegistry.INSTANCE);
    public static final ValueConverter<Platform, String> PLATFORM =
            registryConverter(Platform.class, SupporterRegistry.PLATFORMS);
    public static final ValueConverter<SubscriptionKey, String> SUBSCRIPTION_KEY =
            registryConverter(SubscriptionKey.class, SupporterRegistry.SUBSCRIPTION_TYPES);
    public static final ValueConverter<PurchaseType, String> PURCHASE_TYPE =
            registryConverter(PurchaseType.class, SupporterRegistry.PURCHASE_TYPE);

    private static <V extends Key> ValueConverter<V, String> registryConverter(Class<V> clazz, Registry<V> registry) {
        return ValueConverter.create(
                Adapter.create(clazz, (stmt, index, value) -> stmt.setString(index, value.name()), Types.VARCHAR),
                ValueReader.create(s -> registry.byName(s).orElseThrow(), Row::getString, Row::getString));
    }
}
