/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;

import java.util.HashMap;
import java.util.Map;

public class StringDecoder {
    ///
    /// The Private RSA encoder that was used to encrypt the AES key.
    /// In this case, it has to be the private key and not the public key.
    ///
    private final Decoder rsa;

    private final Map<EncryptedAESAlgorithmWrapper, Decoder> aesDecoder = new HashMap<>();

    public StringDecoder(Decoder rsa) {
        this.rsa = rsa;
    }

    public String decode(EncryptedContent content) {
        Decoder decoder = aesDecoder.computeIfAbsent(content.key(), this::decodeKey);
        return decoder.processFromStringToString(content.content());
    }

    private Decoder decodeKey(EncryptedAESAlgorithmWrapper key) {
        return new Decoder(key.decrypt(rsa));
    }
}
