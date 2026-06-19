/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.core.Bot;
import dev.chojo.phoenix.data.SaduConfig;
import dev.chojo.phoenix.guice.PhoenixModule;
import dev.chojo.phoenix.web.WebService;

import java.io.IOException;
import java.sql.SQLException;

public class Bootstrapper {

    void main() throws InterruptedException, SQLException, IOException {
        Configuration configuration = new Configuration();
        configuration.main();
        configuration.save();

        Injector injector = Guice.createInjector(new PhoenixModule(configuration));

        SaduConfig sadu = injector.getInstance(SaduConfig.class);
        sadu.init();

        Bot instance = injector.getInstance(Bot.class);
        instance.start(injector);

        WebService webService = injector.getInstance(WebService.class);
    }
}
