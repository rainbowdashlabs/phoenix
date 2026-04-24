/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.serialization;

import dev.chojo.phoenix.crypto.processing.Decryptor;
import dev.chojo.phoenix.crypto.processing.Encryptor;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
import dev.chojo.phoenix.crypto.processing.wrapper.SymAlgorithmWrapper;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/// A record representing an encrypted AES algorithm wrapper for serialization.
///
/// @param key    the base64 encoded encrypted AES key
/// @param cipher the AES cipher name
public record EncryptedSymAlgorithmWrapper(String key, String cipher) {
    /// Encrypts an [SymAlgorithmWrapper] into an [EncryptedSymAlgorithmWrapper].
    ///
    /// @param wrapper   the AES algorithm wrapper to encrypt
    /// @param encryptor the encryptor to use for encrypting the AES key
    /// @return the encrypted AES algorithm wrapper
    public static EncryptedSymAlgorithmWrapper encrypt(
            SymAlgorithmWrapper wrapper, Encryptor<BytesProcessInput, BytesProcessResult> encryptor) {
        String key = Base64.getEncoder()
                .encodeToString(encryptor
                        .process(new BytesProcessInput(wrapper.key().getEncoded()))
                        .bytes());
        return new EncryptedSymAlgorithmWrapper(key, wrapper.cipherName());
    }

    /// Decrypts the [EncryptedSymAlgorithmWrapper] back into an [SymAlgorithmWrapper].
    ///
    /// @param decryptor the decryptor to use for decrypting the AES key
    /// @return the decrypted AES algorithm wrapper
    public SymAlgorithmWrapper decrypt(Decryptor<BytesProcessInput, BytesProcessResult> decryptor) {
        byte[] key = decryptor
                .process(new BytesProcessInput(Base64.getDecoder().decode(this.key)))
                .bytes();
        SecretKeySpec aes = new SecretKeySpec(key, 0, key.length, "AES");
        return new SymAlgorithmWrapper(aes, cipher, Cipher.DECRYPT_MODE);
    }
}
