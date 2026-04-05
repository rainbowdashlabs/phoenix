/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.serialization;

import dev.chojo.crypto.processing.Decoder;
import dev.chojo.crypto.processing.Encoder;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;

import javax.crypto.spec.SecretKeySpec;

public record EncryptedAESAlgorithmWrapper(String key, String iv, String cipher) {
    public static EncryptedAESAlgorithmWrapper encrypt(AESAlgorithmWrapper wrapper, Encoder encoder) {
        String key = encoder.processToString(wrapper.key().getEncoded());
        String iv = encoder.processToString(wrapper.iv());
        String cipher = encoder.processToString(wrapper.cipher());
        return new EncryptedAESAlgorithmWrapper(key, iv, cipher);
    }

    public AESAlgorithmWrapper decrypt(Decoder decoder) {
        byte[] key = decoder.processFromString(this.key);
        byte[] iv = decoder.processFromString(this.iv);
        String cipher = decoder.processFromStringToString(this.cipher);
        SecretKeySpec aes = new SecretKeySpec(key, 0, key.length, "AES");
        return new AESAlgorithmWrapper(aes, iv, cipher);
    }
}
