/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.configuration.elements.sub;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.OverwritePrefix;
import dev.chojo.ocular.override.Prop;

@OverwritePrefix("bot.api")
public class Api {
    @Overwrite(env = @Env, prop = @Prop)
    private String host = "0.0.0.0";

    @Overwrite(env = @Env, prop = @Prop)
    private int port = 8888;

    @Overwrite(env = @Env, prop = @Prop)
    private String url = "https://elpis.chojo.dev";

    @Overwrite(env = @Env, prop = @Prop)
    private int tokenValidHours = 720; // 30 Days

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String url() {
        return url;
    }

    public int tokenValidHours() {
        return tokenValidHours;
    }

    public String pathUrl(long guildId, String path) {
        String url = "%s/%s".formatted(url(), path);
        return "%s?guild=%s".formatted(url, guildId);
    }
}
