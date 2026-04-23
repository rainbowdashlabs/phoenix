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

import java.util.Collections;
import java.util.List;

@OverwritePrefix("bot")
public class General {
    @Overwrite(env = @Env("TOKEN"), prop = @Prop("token"))
    private String token;

    @Overwrite(env = @Env, prop = @Prop)
    private boolean testmode = false;

    @Overwrite(env = @Env, prop = @Prop)
    private long botguild = 0L;

    private List<Long> botOwner = Collections.emptyList();

    public String token() {
        return token;
    }

    public boolean testmode() {
        return testmode;
    }

    public long botguild() {
        return botguild;
    }

    public List<Long> botOwner() {
        return botOwner;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }
}
