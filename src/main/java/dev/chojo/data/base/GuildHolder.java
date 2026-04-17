/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.base;

import net.dv8tion.jda.api.entities.Guild;

public interface GuildHolder {
    GuildHolder guildHolder();

    default Guild guild() {
        return guildHolder().guild();
    }

    default long guildId() {
        return guildHolder().guildId();
    }
}
