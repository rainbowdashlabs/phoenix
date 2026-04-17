/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.model.SymProcessInput;
import dev.chojo.crypto.processing.model.SymProcessResult;
import dev.chojo.crypto.serialization.EncryptedSymAlgorithmWrapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/// Decrypts strings using a combination of RSA and AES.
///
/// This class handles the decryption of content using AES, and the decryption of the AES key
/// using RSA.
public class StringDecrypter {
    /// The Private RSA encoder that was used to encrypt the AES key.
    /// In this case, it has to be the private key and not the public key.
    private final Decryptor<BytesProcessInput, BytesProcessResult> rsa;

    private final Map<EncryptedSymAlgorithmWrapper, Decryptor<SymProcessInput, SymProcessResult>> aesDecoder =
            new HashMap<>();

    /// Creates a new string decrypter with the given RSA decryptor.
    ///
    /// @param rsa the RSA decryptor for decrypting the AES keys
    public StringDecrypter(Decryptor<BytesProcessInput, BytesProcessResult> rsa) {
        this.rsa = rsa;
    }

    /// Decodes the given encrypted content.
    ///
    /// @param content the content to decode
    /// @return the decoded string
    public String decode(EncryptedContent content) {
        var decryptor = aesDecoder.computeIfAbsent(content.key(), this::decodeKey);
        byte[] data = Base64.getDecoder().decode(content.content());
        byte[] iv = content.iv() != null ? Base64.getDecoder().decode(content.iv()) : null;

        return new String(decryptor.process(new SymProcessInput(data, iv)).bytes());
    }

    private Decryptor<SymProcessInput, SymProcessResult> decodeKey(EncryptedSymAlgorithmWrapper key) {
        return new Decryptor<>(key.decrypt(rsa));
    }
}
