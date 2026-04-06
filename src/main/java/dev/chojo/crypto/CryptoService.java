/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.sub.Crypto;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/// Service providing cryptographic functionalities such as key generation and configuration access.
public class CryptoService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private final Configuration configuration;
    private final KeyPairGenerator rsaGenerator;
    private final KeyGenerator aesGenerator;

    /// Creates a new crypto service instance.
    ///
    /// @param configuration the configuration to use
    /// @throws NoSuchAlgorithmException if the specified algorithm is not available
    @Inject
    public CryptoService(Configuration configuration) throws NoSuchAlgorithmException {
        this.configuration = configuration;
        rsaGenerator = KeyPairGenerator.getInstance(asymmetricAlgorithm());
        rsaGenerator.initialize(asymmetricKeySize(), secureRandom);
        aesGenerator = KeyGenerator.getInstance(symmetricAlgorithm());
        aesGenerator.init(symmetricKeySize(), secureRandom);
    }

    /// Generates a new RSA key pair.
    ///
    /// @return the generated RSA key pair
    public KeyPair generateRSAKeyPair() {
        return rsaGenerator.generateKeyPair();
    }

    /// Generates a random AES key in encrypt mode.
    ///
    /// @return the generated AES algorithm wrapper
    public AESAlgorithmWrapper randomAESKey() {
        return randomAESKey(Cipher.ENCRYPT_MODE);
    }

    /// Generates a random AES key with the specified operation mode.
    ///
    /// @param opMode the operation mode (e.g., [Cipher#ENCRYPT_MODE])
    /// @return the generated AES algorithm wrapper
    public AESAlgorithmWrapper randomAESKey(int opMode) {
        SecretKey secretKey = aesGenerator.generateKey();
        return new AESAlgorithmWrapper(secretKey, symmetricCipher(), opMode);
    }

    public String asymmetricCipher() {
        return crypto().asymmetricCipher();
    }

    public String asymmetricAlgorithm() {
        return crypto().asymmetricAlgorithm();
    }

    public int asymmetricKeySize() {
        return crypto().asymmetricKeySize();
    }

    public String symmetricCipher() {
        return crypto().symmetricCipher();
    }

    private String symmetricAlgorithm() {
        return crypto().symmetricAlgorithm();
    }

    public Crypto crypto() {
        return configuration.main().crypto();
    }

    public int symmetricKeySize() {
        return crypto().symmetricKeySize();
    }
}
