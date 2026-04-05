/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.serialization;

import dev.chojo.crypto.processing.Decryptor;
import dev.chojo.crypto.processing.Encryptor;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public record EncryptedAESAlgorithmWrapper(String key, String cipher) {
    public static EncryptedAESAlgorithmWrapper encrypt(
            AESAlgorithmWrapper wrapper, Encryptor<BytesProcessInput, BytesProcessResult> encryptor) {
        String key = Base64.getEncoder()
                .encodeToString(encryptor
                        .process(new BytesProcessInput(wrapper.key().getEncoded()))
                        .bytes());
        return new EncryptedAESAlgorithmWrapper(key, wrapper.cipherName());
    }

    public AESAlgorithmWrapper decrypt(Decryptor<BytesProcessInput, BytesProcessResult> decryptor) {
        byte[] key = decryptor
                .process(new BytesProcessInput(Base64.getDecoder().decode(this.key)))
                .bytes();
        SecretKeySpec aes = new SecretKeySpec(key, 0, key.length, "AES");
        return new AESAlgorithmWrapper(aes, cipher, Cipher.DECRYPT_MODE);
    }
}
