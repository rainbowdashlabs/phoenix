/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.configuration;

import dev.chojo.configuration.elements.Root;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Configuration extends Configurations<Root> {
    public static final Key<Root> ROOT = Key.builder(getRootPath(), Root::new).build();

    public Configuration() {
        super(getBasePath(), ROOT, List.of(new YamlDataFormat()), Configuration.class.getClassLoader(), null);
    }

    private static Path getRootPath() {
        return Optional.ofNullable(System.getProperty("bot.config"))
                .or(() -> Optional.ofNullable(System.getenv("BOT_CONFIG")))
                .map(Path::of)
                .orElse(Path.of("config.yml"));
    }

    private static Path getBasePath() {
        return Optional.ofNullable(System.getProperty("bot.config.base"))
                .or(() -> Optional.ofNullable(System.getenv("BOT_CONFIG_BASE")))
                .map(Path::of)
                .orElse(Path.of("config"));
    }
}
