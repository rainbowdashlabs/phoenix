/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.policy.KeyRotationPolicy;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.processing.model.SymProcessResult;
import dev.chojo.crypto.processing.wrapper.SymAlgorithmWrapper;
import dev.chojo.crypto.serialization.EncryptedSymAlgorithmWrapper;
import org.jspecify.annotations.Nullable;

import java.util.Base64;

import javax.security.auth.DestroyFailedException;

/// Encrypts strings using a combination of RSA and AES.
///
/// This class handles the encryption of content using AES, and the encryption of the AES key
/// using RSA. It also supports key rotation based on a [KeyRotationPolicy].
public class StringEncryptor {
    /// The AES encoder that was used to encrypt the content.
    @Nullable
    private Encryptor<? extends ProcessInput, ? extends ProcessResult> aes;
    /// The RSA encrypted AES key that was used to encrypt the content.
    @Nullable
    private EncryptedSymAlgorithmWrapper key;
    /// The RSA encoder that is used to encrypt the AES key.
    private final Encryptor<BytesProcessInput, BytesProcessResult> rsa;
    /// The policy that determines when to rotate the AES key.
    private final KeyRotationPolicy keyRotationPolicy;

    /// Creates a new string encryptor with the given RSA encryptor and key rotation policy.
    ///
    /// @param rsa               the RSA encryptor for encrypting the AES keys
    /// @param keyRotationPolicy the policy for AES key rotation
    public StringEncryptor(Encryptor<BytesProcessInput, BytesProcessResult> rsa, KeyRotationPolicy keyRotationPolicy) {
        this.rsa = rsa;
        this.keyRotationPolicy = keyRotationPolicy;
        // TODO: Rotate the key every x bytes.
    }

    /// Encodes the given content.
    ///
    /// @param content the content to encode
    /// @return the encrypted content
    public EncryptedContent encrypt(String content) {
        ProcessResult result = aes().process(content.getBytes());
        String encrypted = Base64.getEncoder().encodeToString(result.bytes());
        String iv = null;
        if (result instanceof SymProcessResult aesResult && aesResult.iv() != null) {
            iv = Base64.getEncoder().encodeToString(aesResult.iv());
        }
        return new EncryptedContent(encrypted, key, iv);
    }

    private Encryptor<? extends ProcessInput, ? extends ProcessResult> aes() {
        if (aes == null || aes.wrapper().processedBytes() > keyRotationPolicy.rotationBytes()) {
            try {
                if (aes != null) aes.wrapper().destroy();
            } catch (DestroyFailedException e) {
                // Ignore. If it fails, we can't do anything about it.
            }
            aes = keyRotationPolicy.rotationSupplier().get();
            key = EncryptedSymAlgorithmWrapper.encrypt((SymAlgorithmWrapper) aes.wrapper, rsa);
        }
        return aes;
    }
}
