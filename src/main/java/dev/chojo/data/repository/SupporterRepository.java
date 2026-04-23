/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.chojo.data.dao.GuildSupporter;

import java.util.concurrent.TimeUnit;

public class SupporterRepository {
    private final Cache<Long, GuildSupporter> guildSupporters =
            CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
}
