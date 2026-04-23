/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao;

import dev.chojo.aether.supporter.access.Subscriptions;

public class GuildSupporter {
    private final long guildId;
    private final Subscriptions subscriptions;

    public GuildSupporter(long guildId) {
        this.guildId = guildId;
    }
}
