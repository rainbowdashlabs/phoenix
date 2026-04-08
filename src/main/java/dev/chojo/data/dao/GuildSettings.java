/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao;

import dev.chojo.data.base.GuildHolder;
import dev.chojo.data.dao.guildsettings.Crypto;
import net.dv8tion.jda.api.entities.Guild;

public class GuildSettings implements GuildHolder {
    private final Guild guild;
    private final long guildId;
    private final Crypto crypto = new Crypto(this);

    public GuildSettings(Guild guild) {
        this.guild = guild;
        this.guildId = guild.getIdLong();
    }

    public GuildSettings(long guildId) {
        this.guild = null;
        this.guildId = guildId;
    }

    public Crypto crypto() {
        return crypto;
    }

    @Override
    public GuildHolder guildHolder() {
        return this;
    }

    public Guild guild() {
        return guild;
    }

    public long guildId() {
        return guildId;
    }
}
