/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.serialization.EncryptedSymAlgorithmWrapper;
import dev.chojo.data.snapshot.EncryptedMessage;

import java.util.concurrent.ExecutionException;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static java.util.concurrent.TimeUnit.MINUTES;

public class MessageRepository {
    private final Cache<EncryptedSymAlgorithmWrapper, Long> storedKeys =
            CacheBuilder.newBuilder().expireAfterWrite(10, MINUTES).build();

    public void storeMessage(EncryptedMessage encryptedMessage) {
        EncryptedContent content = encryptedMessage.content();
        query("""
                INSERT
                INTO
                    guild_message(guild_id, channel_id, message_id, new_message_id, user_id, key_id, message, nonce)
                VALUES
                    (:guild_id, :channel_id, :message_id, NULL, :user_id, :key_id, :message, :nonce)
                """)
                .single(call().bind("guild_id", encryptedMessage.guildId())
                        .bind("channel_id", encryptedMessage.channelId())
                        .bind("message_id", encryptedMessage.messageId())
                        .bind("user_id", encryptedMessage.author().id())
                        .bind("key_id", storeAndGetKey(content.key(), encryptedMessage.guildId()))
                        .bind("message", content.content())
                        .bind("nonce", content.iv()))
                .insert();

        // TODO: This works, but there might be a more efficient way, instead of inserting the user every time.
        query("""
                INSERT
                INTO
                    guild_user(guild_id, id, username, profile_picture)
                VALUES
                    (?, ?, ?, ?)
                ON CONFLICT(guild_id, id)
                    DO UPDATE
                    SET
                        username        = excluded.username,
                        profile_picture = excluded.profile_picture;
                """)
                .single(call().bind(encryptedMessage.guildId())
                        .bind(encryptedMessage.author().id())
                        .bind(encryptedMessage.author().username())
                        .bind(encryptedMessage.author().profilePicture()))
                .insert();
    }

    public long storeAndGetKey(EncryptedSymAlgorithmWrapper key, long guildId) {
        try {
            return storedKeys.get(key, () -> storeKey(key, guildId));
        } catch (ExecutionException e) {
            // Let it burn
            throw new RuntimeException(e);
        }
    }

    private long storeKey(EncryptedSymAlgorithmWrapper key, long guildId) {
        return query("""
                        WITH new_key_id AS (
                            INSERT INTO guild_message_key (guild_id, encrypted_key, cipher)
                                VALUES(:guild_id, :encrypted_key,:cipher)
                                ON CONFLICT(guild_id, encrypted_key, cipher) DO NOTHING
                                RETURNING id
                                      )
                        SELECT
                                id
                            FROM
                                new_key_id
                            UNION
                            SELECT
                                id
                            FROM
                                guild_message_key
                            WHERE guild_id = :guild_id
                              AND encrypted_key = :encrypted_key
                              AND cipher = :cipher
                """)
                .single(call().bind("guild_id", guildId)
                        .bind("encrypted_key", key.key())
                        .bind("cipher", key.cipher()))
                .map(row -> row.getLong("id"))
                .first()
                .orElseThrow();
    }
}
