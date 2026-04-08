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

import java.io.IOException;
import java.sql.SQLException;

public class Bootstrapper {

    void main() throws InterruptedException, SQLException, IOException {
        Injector injector = Guice.createInjector();

        SaduConfig sadu = injector.getInstance(SaduConfig.class);
        sadu.init();

        Bot instance = injector.getInstance(Bot.class);
        instance.start(injector);
    }
}
