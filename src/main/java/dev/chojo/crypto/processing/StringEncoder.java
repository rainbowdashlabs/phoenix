/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.policy.KeyRotationPolicy;
import dev.chojo.crypto.processing.model.AESProcessResult;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;
import org.jspecify.annotations.Nullable;

import java.util.Base64;

public class StringEncoder {

    /// The AES encoder that was used to encrypt the content.
    @Nullable
    private Encryptor<? extends ProcessInput, ? extends ProcessResult> aes;
    /// The RSA encrypted AES key that was used to encrypt the content.
    @Nullable
    private EncryptedAESAlgorithmWrapper key;
    /// The RSA encoder that is used to encrypt the AES key.
    private final Encryptor<BytesProcessInput, BytesProcessResult> rsa;
    /// The policy that determines when to rotate the AES key.
    private final KeyRotationPolicy keyRotationPolicy;

    public StringEncoder(Encryptor<BytesProcessInput, BytesProcessResult> rsa, KeyRotationPolicy keyRotationPolicy) {
        this.rsa = rsa;
        this.keyRotationPolicy = keyRotationPolicy;
        // TODO: Rotate the key every x bytes
    }

    public EncryptedContent encode(String content) {
        ProcessResult result = aes().process(content.getBytes());
        String encrypted = Base64.getEncoder().encodeToString(result.bytes());
        String iv = null;
        if (result instanceof AESProcessResult aesResult && aesResult.iv() != null) {
            iv = Base64.getEncoder().encodeToString(aesResult.iv());
        }
        return new EncryptedContent(encrypted, key, iv);
    }

    private Encryptor<? extends ProcessInput, ? extends ProcessResult> aes() {
        if (aes == null || aes.wrapper().processedBytes() > keyRotationPolicy.rotationBytes()) {
            aes = keyRotationPolicy.rotationSupplier().get();
            key = EncryptedAESAlgorithmWrapper.encrypt((AESAlgorithmWrapper) aes.wrapper, rsa);
        }
        return aes;
    }
}
