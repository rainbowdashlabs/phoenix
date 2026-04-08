/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.guice;

import com.google.inject.AbstractModule;
import dev.chojo.commands.message.Replicate;
import dev.chojo.configuration.Configuration;
import dev.chojo.crypto.CryptoService;
import dev.chojo.data.SaduConfig;

public class ElpisModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(new Configuration());
        bind(CryptoService.class);
        bind(SaduConfig.class);
        bind(Replicate.class);
    }
}
