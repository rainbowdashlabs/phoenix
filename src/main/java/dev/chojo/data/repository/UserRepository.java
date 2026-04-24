/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import dev.chojo.data.dao.user.PUser;
import dev.chojo.data.dao.user.sub.token.UserToken;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class UserRepository {
    private final Cache<Long, PUser> users =
            CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).build();

    @Inject
    public UserRepository() {}

    public PUser byId(long id) {
        try {
            return users.get(id, () -> new PUser(id));
        } catch (ExecutionException e) {
            return new PUser(id);
        }
    }

    public PUser byUser(User user) {
        return byId(user.getIdLong());
    }

    public Optional<PUser> byMailHash(String hash) {
        return query("""
                SELECT
                    user_id
                FROM
                    user_mails um
                WHERE mail_hash = ?;
                """).single(call().bind(hash)).mapAs(Long.class).first().map(this::byId);
    }

    public void cleanupExpiredMails() {
        query("""
                DELETE
                FROM
                    user_mails
                WHERE verification_requested < now() - INTERVAL '1 hour'
                  AND NOT verified;
                """).single().delete();
    }

    public List<UserToken> getExpiringTokens(Instant cutoff) {
        return query("""
                SELECT
                    user_id,
                    access_token,
                    refresh_token,
                    expiry
                FROM
                    user_token
                WHERE expiry < ?;
                """)
                .single(call().bind(cutoff, StandardValueConverter.INSTANT_TIMESTAMP))
                .mapAs(UserToken.class)
                .all();
    }

    public Optional<PUser> byToken(String token) {
        return query("""
                SELECT
                    user_id,
                    access_token,
                    refresh_token,
                    expiry,
                    token,
                    last_used
                FROM
                    user_token
                WHERE token = ?;
                """)
                .single(call().bind(token))
                .map(row -> row.getLong("user_id"))
                .first()
                .map(this::byId);
    }
}
