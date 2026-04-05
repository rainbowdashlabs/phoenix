/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.configuration.elements.sub;

import dev.chojo.ocular.override.OverwritePrefix;

@OverwritePrefix("bot.crypto")
public class Crypto {
    private String rsaKey = "RSA";
    private int rsaKeySize = 2048;
    private String rsaCipher = "RSA/ECB/PKCS1Padding";

    private String aesCipher = "AES/GCM/NoPadding";
    private int aesKeySize = 256;
    private int aesIterations = 65536;

    public String rsaKey() {
        return rsaKey;
    }

    public int rsaKeySize() {
        return rsaKeySize;
    }

    public String rsaCipher() {
        return rsaCipher;
    }

    public String aesCipher() {
        return aesCipher;
    }

    public int aesKeySize() {
        return aesKeySize;
    }

    public int aesIterations() {
        return aesIterations;
    }
}
