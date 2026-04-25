/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.configuration.elements;

import dev.chojo.aether.discordoauth.configuration.DiscordOAuth;
import dev.chojo.aether.kofi.configuration.Kofi;
import dev.chojo.aether.mailing.configuration.Mailing;
import dev.chojo.aether.supporter.configuration.SupporterConfiguration;
import dev.chojo.phoenix.configuration.elements.sub.Api;
import dev.chojo.phoenix.configuration.elements.sub.Crypto;
import dev.chojo.phoenix.configuration.elements.sub.Database;
import dev.chojo.phoenix.configuration.elements.sub.General;
import dev.chojo.phoenix.configuration.elements.sub.Links;
import dev.chojo.phoenix.configuration.elements.sub.supporter.FeatureID;
import dev.chojo.phoenix.configuration.elements.sub.supporter.FeatureMeta;
import dev.chojo.phoenix.configuration.elements.sub.supporter.FeaturePrice;

@SuppressWarnings("FieldMayBeFinal")
public class Root {
    private General general = new General();
    private Crypto crypto = new Crypto();
    private Api api = new Api();
    private Database database = new Database();
    private DiscordOAuth discordOAuth = new DiscordOAuth();
    private Kofi kofi = new Kofi();
    private Mailing mailing = new Mailing();
    private SupporterConfiguration<FeatureID, FeaturePrice, FeatureMeta> supporter = new SupporterConfiguration<>();
    private Links links = new Links();

    public Links links() {
        return links;
    }

    public General general() {
        return general;
    }

    public Crypto crypto() {
        return crypto;
    }

    public Api api() {
        return api;
    }

    public DiscordOAuth discordOAuth() {
        return discordOAuth;
    }

    public SupporterConfiguration<FeatureID, FeaturePrice, FeatureMeta> supporter() {
        return supporter;
    }

    public Kofi kofi() {
        return kofi;
    }

    public Mailing mailing() {
        return mailing;
    }

    public Database database() {
        return database;
    }
}
