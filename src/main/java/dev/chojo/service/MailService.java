package dev.chojo.service;

import dev.chojo.aether.mailing.entities.MailSource;
import dev.chojo.aether.mailing.service.AMailService;
import dev.chojo.aether.mailing.service.MailServiceConfig;
import dev.chojo.data.dao.user.sub.MailEntry;

import java.time.Instant;

public class MailService extends AMailService {
    /**
     * Create a new mail service with the given configuration.
     *
     * @param config the configuration
     */
    public MailService(MailServiceConfig config) {
        super(config);
    }

    @Override
    protected MailEntry createMailEntry(long user, MailSource source, String mailHash, String mailShort, boolean verified, Instant verificationRequested, String verificationCode) {
        return new MailEntry(user, source, mailHash, mailShort, verified, verificationRequested, verificationCode);
    }
}
