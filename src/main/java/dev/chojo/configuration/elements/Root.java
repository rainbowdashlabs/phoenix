/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.configuration.elements;

import dev.chojo.configuration.elements.sub.Api;
import dev.chojo.configuration.elements.sub.Database;
import dev.chojo.configuration.elements.sub.General;

public class Root {
    private General general = new General();
    private Api api = new Api();
    private Database database = new Database();

    public General general() {
        return general;
    }

    public Api api() {
        return api;
    }

    public Database database() {
        return database;
    }
}
