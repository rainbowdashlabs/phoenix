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

public class CryptoService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private final Configuration configuration;
    private final KeyPairGenerator rsaGenerator;
    private final KeyGenerator aesGenerator;

    @Inject
    public CryptoService(Configuration configuration) throws NoSuchAlgorithmException {
        this.configuration = configuration;
        rsaGenerator = KeyPairGenerator.getInstance(asymmetricAlgorithm());
        rsaGenerator.initialize(asymmetricKeySize(), secureRandom);
        aesGenerator = KeyGenerator.getInstance(crypto().symmetricAlgorithm());
        aesGenerator.init(aesKeySize(), secureRandom);
    }

    public KeyPair generateRSAKeyPair() {
        return rsaGenerator.generateKeyPair();
    }

    public AESAlgorithmWrapper randomAESKey() {
        return randomAESKey(Cipher.ENCRYPT_MODE);
    }

    public AESAlgorithmWrapper randomAESKey(int opMode) {
        SecretKey secretKey = aesGenerator.generateKey();
        return new AESAlgorithmWrapper(secretKey, aesCipher(), opMode);
    }

    String rsaCipher() {
        return configuration.main().crypto().asymmetricCipher();
    }

    String asymmetricAlgorithm() {
        return configuration.main().crypto().asymmetricAlgorithm();
    }

    int asymmetricKeySize() {
        return configuration.main().crypto().asymmetricKeySize();
    }

    public String aesCipher() {
        return configuration.main().crypto().symmetricCipher();
    }

    private Crypto crypto() {
        return configuration.main().crypto();
    }

    int aesKeySize() {
        return crypto().symmetricKeySize();
    }
}
