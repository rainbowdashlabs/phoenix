/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao.user.sub.token;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import dev.chojo.aether.discordoauth.access.IOAuthToken;
import dev.chojo.aether.discordoauth.access.OAuthScope;
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

public final class UserToken implements IOAuthToken {
    private final long userId;
    private String accessToken;
    private String refreshToken;
    private Instant expiry;
    private final Set<OAuthScope> scopes;
    private final String token;
    private Instant lastUsed;

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
            long userId,
            String accessToken,
            String refreshToken,
            Instant expiry,
            Set<OAuthScope> scope,
            String token,
            Instant lastUsed) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiry = expiry;
        this.scopes = scope;
        this.token = token;
        this.lastUsed = lastUsed;
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
                row.getString("token"),
                row.get("last_used", INSTANT_TIMESTAMP));
    }

    public void delete() {
        query("DELETE FROM user_token WHERE user_id = ?")
                .single(call().bind(userId))
                .delete();
    }

    public Instant lastUsed() {
        return lastUsed;
    }

    public void used() {
        query("UPDATE user_token SET last_used = now() WHERE token = ?")
                .single(call().bind(token))
                .update()
                .ifChanged(e -> lastUsed = Instant.now());
    }

    @Override
    public long userId() {
        return userId;
    }

    @Override
    public void update(TokenResponse response) {
        query("""
                UPDATE user_token SET access_token = ?, refresh_token = ?, expiry = ? WHERE token = ?;
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
