/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao.user.sub;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import dev.chojo.aether.discordoauth.access.OAuthScope;
import dev.chojo.aether.discordoauth.access.OAuthToken;
import dev.chojo.aether.discordoauth.pojo.TokenResponse;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public final class UserToken implements OAuthToken {
    private final long userId;
    private String accessToken;
    private String refreshToken;
    private Instant expiry;
    private final Set<OAuthScope> scopes;
    private final String token;

    /**
     * Create a new WebToken.
     *
     * @param userId       user id
     * @param accessToken  access token
     * @param refreshToken refresh token
     * @param expiry       token expiry
     * @param token        token
     */
    public UserToken(
            long userId, String accessToken, String refreshToken, Instant expiry, Set<OAuthScope> scope, String token) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiry = expiry;
        this.scopes = scope;
        this.token = token;
    }

    @MappingProvider({"user_id", "access_token", "refresh_token", "expiry", "token"})
    public UserToken(Row row) throws SQLException {
        List<String> scopes = row.getList("scopes");
        this(
                row.getLong("user_id"),
                row.getString("access_token"),
                row.getString("refresh_token"),
                row.get("expiry", INSTANT_TIMESTAMP),
                scopes.stream().map(OAuthScope::valueOf).collect(Collectors.toSet()),
                row.getString("token"));
    }

    public void delete() {
        query("DELETE FROM user_token WHERE user_id = ?")
                .single(call().bind(userId))
                .delete();
    }

    @Override
    public long userId() {
        return userId;
    }

    @Override
    public void update(TokenResponse response) {
        query("""
                INSERT
                INTO
                    user_token
                    (user_id, access_token, refresh_token, expiry, token)
                VALUES
                    (?, ?, ?, ?, ?)
                ON CONFLICT (token)
                    DO UPDATE
                    SET
                        access_token  = excluded.access_token,
                        refresh_token = excluded.refresh_token,
                        expiry        = excluded.expiry
                """)
                .single(call().bind(userId)
                        .bind(response.accessToken())
                        .bind(response.refreshToken())
                        .bind(response.expiry(), INSTANT_TIMESTAMP)
                        .bind(token()))
                .insert()
                .ifChanged(e -> {
                    this.accessToken = response.accessToken();
                    this.refreshToken = response.refreshToken();
                    this.expiry = response.expiry();
                });
    }

    @Override
    public String refreshToken() {
        return refreshToken;
    }

    @Override
    public Set<OAuthScope> scopes() {
        return scopes;
    }

    public String accessToken() {
        return accessToken;
    }

    public Instant expiry() {
        return expiry;
    }

    public String token() {
        return token;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserToken) obj;
        return this.userId == that.userId()
                && Objects.equals(this.accessToken, that.accessToken())
                && Objects.equals(this.refreshToken, that.refreshToken())
                && Objects.equals(this.expiry, that.expiry())
                && Objects.equals(this.token, that.token());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, accessToken, refreshToken, expiry, token);
    }

    @Override
    public String toString() {
        return "UserToken[" + "userId="
                + userId + ", " + "accessToken="
                + accessToken + ", " + "refreshToken="
                + refreshToken + ", " + "expiry="
                + expiry + ", " + "token="
                + token + ']';
    }
}
