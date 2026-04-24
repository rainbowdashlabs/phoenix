/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao.user.sub.token;

import de.chojo.sadu.postgresql.types.PostgreSqlTypes;
import dev.chojo.aether.discordoauth.access.OAuthScope;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class UserTokens {
    @Nullable
    private Map<String, UserToken> tokens = null;

    private final long userId;

    public UserTokens(long userId) {
        this.userId = userId;
    }

    public Map<String, UserToken> tokens() {
        if (tokens == null) {
            tokens = new HashMap<>();
            query("""
                    SELECT
                        user_id,
                        access_token,
                        refresh_token,
                        expiry,
                        token,
                        last_used,
                        scopes
                    FROM
                        user_token
                    WHERE user_id = ?
                    """).single(call().bind(userId)).map(UserToken::new).all().forEach(t -> tokens.put(t.token(), t));
        }
        return tokens;
    }

    public Optional<UserToken> token(String token) {
        return Optional.ofNullable(tokens().get(token));
    }

    public UserToken addToken(UserToken token) {
        return query("""
                INSERT
                INTO
                    user_token
                    (user_id, access_token, refresh_token, expiry, token, scopes)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                ON CONFLICT(token)
                    DO UPDATE
                    SET last_used = now()
                RETURNING user_id, access_token, refresh_token, expiry, token, scopes, last_used;""")
                .single(call().bind(userId)
                        .bind(token.accessToken())
                        .bind(token.refreshToken())
                        .bind(token.expiry(), INSTANT_TIMESTAMP)
                        .bind(token.token())
                        .bind(token.scopes().stream().map(OAuthScope::name).toList(), PostgreSqlTypes.TEXT))
                .map(UserToken::new)
                .first()
                .filter(e -> e.userId() == token.userId())
                .map(e -> {
                    tokens().put(e.token(), e);
                    return e;
                })
                .orElseThrow();
    }

    public void removeToken(String token) {
        tokens().remove(token).delete();
    }

    public void updateToken(UserToken token) {
        query("""
                UPDATE user_token SET access_token = ?, refresh_token = ?, expiry = ? WHERE token = ?;
                """)
                .single(call().bind(token.accessToken())
                        .bind(token.refreshToken())
                        .bind(token.expiry(), INSTANT_TIMESTAMP)
                        .bind(token.token()))
                .update()
                .ifChanged(e -> tokens().put(token.token(), token));
    }
}
