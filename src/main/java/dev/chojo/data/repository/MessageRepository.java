/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;
import dev.chojo.data.snapshot.EncryptedMessage;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class MessageRepository {

    public void storeMessage(EncryptedMessage encryptedMessage) {
        EncryptedContent content = encryptedMessage.content();
        EncryptedAESAlgorithmWrapper key = content.key();
        query("""
                WITH
                    new_key_id AS (
                        INSERT INTO guild_message_key (guild_id, encrypted_key, cipher)
                            VALUES(:guild_id, :encrypted_key,:cipher)
                            ON CONFLICT(guild_id, encrypted_key, cipher) DO NOTHING
                            RETURNING id
                                  ),
                    key_id AS (
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
                                  )
                INSERT
                INTO
                    guild_message(guild_id, channel_id, message_id, new_message_id, user_id, key_id, message, nonce)
                VALUES
                    (:guild_id, :channel_id, :message_id, NULL, :user_id, (SELECT id FROM key_id), :message, :nonce)
                """)
                .single(call().bind("guild_id", encryptedMessage.guildId())
                        .bind("encrypted_key", key.key())
                        .bind("cipher", key.cipher())
                        .bind("channel_id", encryptedMessage.channelId())
                        .bind("message_id", encryptedMessage.messageId())
                        .bind("user_id", encryptedMessage.author().id())
                        .bind("message", content.content())
                        .bind("nonce", content.iv()))
                .insert();
    }
}
