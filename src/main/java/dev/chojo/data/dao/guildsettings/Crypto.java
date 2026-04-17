/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao.guildsettings;

import dev.chojo.crypto.processing.wrapper.AsymAlgorithmWrapper;
import dev.chojo.crypto.serialization.PlainAsymAlgorithmWrapper;
import dev.chojo.data.base.GuildHolder;
import dev.chojo.data.dao.GuildSettings;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Crypto implements GuildHolder {
    private final GuildSettings guildSettings;

    @Nullable
    private AsymAlgorithmWrapper wrapper;

    private boolean initialized = false;

    public Crypto(GuildSettings guildSettings) {
        this.guildSettings = guildSettings;
    }

    public synchronized boolean setPublicKey(PlainAsymAlgorithmWrapper wrapper) {
        Objects.requireNonNull(wrapper, "Wrapper cannot be null");
        if (this.wrapper != null) return false;
        return query("""
                INSERT
                INTO
                    guild_crypto(guild_id, public_key, cipher)
                VALUES
                    (?, ?, ?)
                ON CONFLICT(guild_id) DO UPDATE SET
                    public_key = excluded.public_key, cipher = excluded.cipher""")
                .single(call().bind(guildId()).bind(wrapper.key()).bind(wrapper.cipher()))
                .insert()
                .ifChanged(_ -> {
                    this.wrapper = wrapper.unwrap();
                    this.initialized = true;
                });
    }

    public synchronized void clearPublicKey() {
        query("""
                UPDATE guild_crypto SET public_key = NULL, cipher = NULL
                WHERE guild_id = ?""").single(call().bind(guildId())).update().ifChanged(_ -> {
            this.initialized = false;
            this.wrapper = null;
        });
    }

    public @Nullable AsymAlgorithmWrapper publicKey() {
        if (wrapper == null && !initialized) {
            query("""
                    SELECT
                        guild_id,
                        public_key,
                        cipher
                    FROM
                        guild_crypto
                    WHERE guild_id = ? AND public_key IS NOT NULL AND cipher IS NOT NULL""")
                    .single(call().bind(guildId()))
                    .map(row -> new PlainAsymAlgorithmWrapper(row.getString("public_key"), row.getString("cipher")))
                    .first()
                    .map(PlainAsymAlgorithmWrapper::unwrap)
                    .ifPresent(wrapper -> this.wrapper = wrapper);
            initialized = true;
        }
        return wrapper;
    }

    public boolean hasPublicKey() {
        if (!initialized) publicKey();
        return this.wrapper != null;
    }

    @Override
    public GuildHolder guildHolder() {
        return guildSettings;
    }
}
