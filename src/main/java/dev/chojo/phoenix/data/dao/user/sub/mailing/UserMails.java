/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.dao.user.sub.mailing;

import dev.chojo.aether.mailing.IUserMails;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class UserMails implements IUserMails<UserMail> {
    private final long userId;

    @Nullable
    private Map<String, UserMail> mails;

    public UserMails(long userId) {
        this.userId = userId;
    }

    public Map<String, UserMail> mails() {
        if (mails == null) {
            mails = new HashMap<>();
            query("""
                    SELECT
                        user_id,
                        source,
                        mail_hash,
                        mail_short,
                        verified,
                        verification_requested,
                        verification_code
                    FROM
                        user_mails
                    WHERE user_id = ?;
                    """).single(call().bind(userId)).mapAs(UserMail.class).all().forEach(m -> mails.put(m.hash(), m));
        }
        return mails;
    }

    @Override
    public long userId() {
        return userId;
    }

    public Optional<UserMail> getMail(String hash) {
        return Optional.ofNullable(mails().get(hash));
    }

    @Override
    public void addMail(UserMail userMail) {
        query("""
                INSERT INTO user_mails (user_id, source, mail_hash, mail_short, verified, verification_requested, verification_code) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING;
                """)
                .single(call().bind(userId)
                        .bind(userMail.source().name())
                        .bind(userMail.hash())
                        .bind(userMail.mailShort())
                        .bind(userMail.verified())
                        .bind(userMail.verificationRequested(), INSTANT_TIMESTAMP)
                        .bind(userMail.verificationCode()))
                .insert()
                .ifChanged(i -> mails().put(userMail.hash(), userMail));
    }

    public boolean removeMail(String mailHash) {
        return query("""
                DELETE FROM user_mails WHERE user_id = ? AND mail_hash = ?;
                """).single(call().bind(userId).bind(mailHash)).update().ifChanged(ii -> mails().remove(mailHash));
    }
}
