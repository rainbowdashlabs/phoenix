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

@OverwritePrefix("bot.db")
public class Database {
    @Overwrite(env = @Env, prop = @Prop)
    private String host = "localhost";

    @Overwrite(env = @Env, prop = @Prop)
    private String port = "5432";

    @Overwrite(env = @Env, prop = @Prop)
    private String database = "postgres";

    @Overwrite(env = @Env, prop = @Prop)
    private String schema = "elpis_schema";

    @Overwrite(env = @Env, prop = @Prop)
    private String user = "postgres";

    @Overwrite(env = @Env, prop = @Prop)
    private String password = "postgres";

    @Overwrite(env = @Env, prop = @Prop)
    private int poolSize = 5;

    public String host() {
        return host;
    }

    public String port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String schema() {
        return schema;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public int poolSize() {
        return poolSize;
    }
}
