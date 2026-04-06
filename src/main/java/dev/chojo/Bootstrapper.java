/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.chojo.core.Bot;
import dev.chojo.data.SaduConfig;
import dev.chojo.guice.ElpisModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class Bootstrapper {

    private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    void main() throws InterruptedException, SQLException, IOException {
        Injector injector = Guice.createInjector(new ElpisModule());
        SaduConfig sadu = injector.getInstance(SaduConfig.class);
        sadu.init();
        Bot instance = injector.getInstance(Bot.class);
        instance.start();
    }
}
