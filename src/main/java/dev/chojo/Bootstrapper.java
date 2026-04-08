/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo;

import dev.chojo.configuration.Configuration;
import dev.chojo.core.Bot;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class Bootstrapper {

    void main() throws InterruptedException, SQLException, IOException, NoSuchAlgorithmException {
        new Bot(new Configuration()).start();
    }
}
