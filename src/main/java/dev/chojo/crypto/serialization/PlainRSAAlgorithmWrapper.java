/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.serialization;

import dev.chojo.crypto.exceptions.CryptoException;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/// A record representing a plain RSA algorithm wrapper for serialization.
///
/// @param key    the PEM encoded RSA key
/// @param cipher the RSA cipher name
public record PlainRSAAlgorithmWrapper(String key, String cipher) {
    /// Wraps an [RSAAlgorithmWrapper] into a [PlainRSAAlgorithmWrapper].
    ///
    /// @param wrapper the RSA algorithm wrapper to wrap
    /// @return the wrapped RSA algorithm wrapper
    /// @throws CryptoException if the key type is unsupported
    public static PlainRSAAlgorithmWrapper wrap(RSAAlgorithmWrapper wrapper) {
        String encoded = Base64.getEncoder().encodeToString(wrapper.key().getEncoded());
        String key;
        if (wrapper.key() instanceof PublicKey) {
            key = "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
        } else if (wrapper.key() instanceof PrivateKey) {
            key = "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----";
        } else {
            throw new CryptoException(
                    "Unsupported key type: " + wrapper.key().getClass().getName(), null);
        }
        return new PlainRSAAlgorithmWrapper(key, wrapper.cipherName());
    }

    /// Unwraps the [PlainRSAAlgorithmWrapper] back into an [RSAAlgorithmWrapper].
    ///
    /// @return the unwrapped RSA algorithm wrapper
    /// @throws CryptoException if the RSA key format is invalid or decryption fails
    public RSAAlgorithmWrapper unwrap() {
        try {
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            if (this.key.contains("-----BEGIN PUBLIC KEY-----")) {
                String keyStr = this.key
                        .replace("\n", "")
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .trim();
                byte[] decoded = decode(keyStr);
                PublicKey publicKey = rsaFactory.generatePublic(new X509EncodedKeySpec(decoded));
                return new RSAAlgorithmWrapper(publicKey, this.cipher, Cipher.ENCRYPT_MODE);
            } else if (this.key.contains("-----BEGIN PRIVATE KEY-----")) {
                String keyStr = this.key
                        .replace("\n", "")
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .trim();
                byte[] decoded = decode(keyStr);
                PrivateKey privateKey = rsaFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
                return new RSAAlgorithmWrapper(privateKey, this.cipher, Cipher.DECRYPT_MODE);
            } else {
                throw new CryptoException("Invalid RSA key format", null);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException("Could not decrypt RSA wrapper", e);
        }
    }

    private byte[] decode(String keyStr) {
        try {
            return Base64.getDecoder().decode(keyStr);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Invalid base64 in RSA key", e);
        }
    }
}
