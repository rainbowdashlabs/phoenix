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

@OverwritePrefix("bot")
public class General {
    @Overwrite(env = @Env("TOKEN"), prop = @Prop("token"))
    private String token;

    public String token() {
        return token;
    }
}
