/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.processing.model.AESProcessInput;
import dev.chojo.crypto.processing.model.AESProcessResult;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class StringDecoder {
    /// The Private RSA decryptor that was used to encrypt the AES key.
    /// In this case, it has to be the private key and not the public key.
    private final Decryptor<BytesProcessInput, BytesProcessResult> rsa;

    private final Map<EncryptedAESAlgorithmWrapper, Decryptor<? extends ProcessInput, ? extends ProcessResult>>
            aesDecoder = new HashMap<>();

    public StringDecoder(Decryptor<BytesProcessInput, BytesProcessResult> rsa) {
        this.rsa = rsa;
    }

    public String decode(EncryptedContent content) {
        var decryptor = aesDecoder.computeIfAbsent(content.key(), this::decodeKey);
        byte[] data = Base64.getDecoder().decode(content.content());
        byte[] iv = content.iv() != null ? Base64.getDecoder().decode(content.iv()) : null;

        if (decryptor.wrapper() instanceof dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper) {
            @SuppressWarnings("unchecked")
            Decryptor<AESProcessInput, AESProcessResult> aesDecryptor =
                    (Decryptor<AESProcessInput, AESProcessResult>) decryptor;
            return new String(
                    aesDecryptor.process(new AESProcessInput(data, iv)).bytes());
        }

        return new String(decryptor.process(data).bytes());
    }

    private Decryptor<AESProcessInput, AESProcessResult> decodeKey(EncryptedAESAlgorithmWrapper key) {
        return new Decryptor<>(key.decrypt(rsa));
    }
}
