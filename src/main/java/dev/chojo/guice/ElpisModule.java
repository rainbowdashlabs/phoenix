/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.guice;

import dev.chojo.configuration.Configuration;

public class ElpisModule extends com.google.inject.AbstractModule {

    private Configuration configuration;

    public ElpisModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(configuration);
    }
}
