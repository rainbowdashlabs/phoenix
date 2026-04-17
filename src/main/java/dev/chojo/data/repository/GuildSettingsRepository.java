/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import dev.chojo.data.dao.GuildSettings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuildSettingsRepository {
    private final ShardManager shardManager;
    private final Cache<Long, GuildSettings> cache =
            CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();

    @Inject
    public GuildSettingsRepository(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public GuildSettings get(Guild guild) {
        try {
            return cache.get(guild.getIdLong(), () -> create(guild));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public GuildSettings get(long guildId) {
        try {
            return cache.get(guildId, () -> create(guildId));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private GuildSettings create(long guildId) {
        Guild guildById = shardManager.getGuildById(guildId);
        return guildById == null ? new GuildSettings(guildId) : create(guildById);
    }

    private GuildSettings create(Guild guild) {
        return new GuildSettings(guild);
    }
}
