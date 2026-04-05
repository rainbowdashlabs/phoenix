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

    private String key = "RSA";
    private int keySize = 512;
    private String cipher = "RSA/ECB/PKCS1Padding";

    public String token() {
        return token;
    }

    public String cipher() {
        return cipher;
    }

    public String key() {
        return key;
    }

    public int keySize() {
        return keySize;
    }
}
