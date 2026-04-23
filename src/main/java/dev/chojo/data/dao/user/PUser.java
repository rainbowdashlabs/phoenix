/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.dao.user;

import dev.chojo.data.dao.user.sub.Purchases;
import dev.chojo.data.dao.user.sub.UserMails;
import dev.chojo.data.dao.user.sub.UserSettings;
import dev.chojo.data.dao.user.sub.UserTokens;
import org.jspecify.annotations.Nullable;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class PUser {
    private final long userId;
    @Nullable
    private UserSettings settings;
    @Nullable
    private UserMails mails;
    @Nullable
    private Purchases purchases;
    @Nullable
    private UserTokens tokens;

    public PUser(long userId) {
        this.userId = userId;
    }

    public UserTokens tokens() {
        if(tokens == null){
            tokens = new UserTokens(userId);
        }
        return tokens;
    }

    public UserSettings settings() {
        if (settings == null) {
            settings = query("""
                    SELECT id, vote_guild FROM user_settings WHERE id = ?
                    """)
                    .single(call().bind(userId))
                    .mapAs(UserSettings.class)
                    .first()
                    .orElseGet(() -> new UserSettings(userId, 0));
        }
        return settings;
    }

    public UserMails mails() {
        if (mails == null) {
            mails = new UserMails(userId);
        }
        return mails;
    }

    public Purchases purchases() {
        if (purchases == null) {
            purchases = new Purchases(userId);
        }
        return purchases;
    }

    public long id() {
        return userId;
    }
}
