/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.chojo.data.snapshot.UserProfile;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class UserProfileRepository {
    private final Cache<Long, Cache<Long, UserProfile>> cache =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    public UserProfile getGuildUser(long guildId, long userId) {
        try {
            return getCache(guildId).get(userId, () -> retrieveUser(guildId, userId));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private UserProfile retrieveUser(long guildId, long userId) {
        return query("SELECT * FROM guild_user WHERE guild_id = :guild_id AND id = :user_id")
                .single(call().bind("guild_id", guildId).bind("user_id", userId))
                .mapAs(UserProfile.class)
                .first()
                .orElseGet(() -> UserProfile.create(userId));
    }

    public void storeGuildUser(long guildId, UserProfile user) {
        UserProfile guildUser = getGuildUser(guildId, user.id());
        if (guildUser.equals(user)) return;
        query("""
                INSERT
                INTO
                    guild_user(guild_id, id, username, profile_picture)
                VALUES (:guild_id, :id, :username, :profile_picture)""")
                .single(call().bind("guild_id", guildId)
                        .bind("id", user.id())
                        .bind("username", user.username())
                        .bind("profile_picture", user.profilePicture()))
                .insert()
                .ifChanged(_ -> getCache(guildId).put(user.id(), user));
    }

    private Cache<Long, UserProfile> getCache(long guildId) {
        try {
            return cache.get(guildId, () -> CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .build());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
