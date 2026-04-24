/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.service;

import com.google.inject.Inject;
import dev.chojo.aether.mailing.entities.MailSource;
import dev.chojo.aether.mailing.service.AMailService;
import dev.chojo.aether.mailing.service.MailServiceConfig;
import dev.chojo.phoenix.data.dao.user.sub.mailing.UserMail;

import java.time.Instant;

public class MailService extends AMailService<UserMail> {
    /**
     * Create a new mail service with the given configuration.
     *
     * @param config the configuration
     */
    @Inject
    public MailService(MailServiceConfig<UserMail> config) {
        super(config);
    }

    @Override
    protected UserMail createMailEntry(
            long user,
            MailSource source,
            String mailHash,
            String mailShort,
            boolean verified,
            Instant verificationRequested,
            String verificationCode) {
        return new UserMail(user, source, mailHash, mailShort, verified, verificationRequested, verificationCode);
    }
}
