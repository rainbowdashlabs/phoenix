/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.configuration.elements;

import dev.chojo.configuration.elements.sub.Api;
import dev.chojo.configuration.elements.sub.Crypto;
import dev.chojo.configuration.elements.sub.Database;
import dev.chojo.configuration.elements.sub.General;

public class Root {
    private General general = new General();
    private Crypto crypto = new Crypto();
    private Api api = new Api();
    private Database database = new Database();

    public General general() {
        return general;
    }

    public Crypto crypto() {
        return crypto;
    }

    public Api api() {
        return api;
    }

    public Database database() {
        return database;
    }
}
