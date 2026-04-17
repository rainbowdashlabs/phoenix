/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.configuration.elements.sub;

import dev.chojo.ocular.override.OverwritePrefix;

@OverwritePrefix("bot.crypto")
public class Crypto {
    private String asymmetricAlgorithm = "RSA";
    private int asymmetricKeySize = 2048;
    private String asymmetricCipher = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private String symmetricAlgorithm = "AES";
    private String symmetricCipher = "AES/GCM/NoPadding";
    private int symmetricKeySize = 256;
    private int rotationInterval = 8000;

    public String asymmetricAlgorithm() {
        return asymmetricAlgorithm;
    }

    public int asymmetricKeySize() {
        return asymmetricKeySize;
    }

    public String asymmetricCipher() {
        return asymmetricCipher;
    }

    public String symmetricCipher() {
        return symmetricCipher;
    }

    public int symmetricKeySize() {
        return symmetricKeySize;
    }

    public String symmetricAlgorithm() {
        return symmetricAlgorithm;
    }

    public long rotationInterval() {
        return rotationInterval;
    }
}
