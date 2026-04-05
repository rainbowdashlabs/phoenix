/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.serialization;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.processing.Decoder;
import dev.chojo.crypto.processing.Encoder;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptedAESAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void encrypt() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        RSAAlgorithmWrapper rsaWrapper = new RSAAlgorithmWrapper(rsa.getPublic());
        AESAlgorithmWrapper aesWrapper = cryptoService.randomAESKey();
        Encoder encoder = new Encoder(rsaWrapper);
        EncryptedAESAlgorithmWrapper wrapper = EncryptedAESAlgorithmWrapper.encrypt(aesWrapper, encoder);
        System.out.printf("Encrypted: %s%n", wrapper);
    }

    @Test
    void decrypt() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        RSAAlgorithmWrapper rsaPublic = new RSAAlgorithmWrapper(rsa.getPublic());
        RSAAlgorithmWrapper rsaPrivate = new RSAAlgorithmWrapper(rsa.getPrivate());
        AESAlgorithmWrapper aesWrapper = cryptoService.randomAESKey();
        Encoder encoder = new Encoder(rsaPublic);
        Decoder decoder = new Decoder(rsaPrivate);
        EncryptedAESAlgorithmWrapper wrapper = EncryptedAESAlgorithmWrapper.encrypt(aesWrapper, encoder);
        AESAlgorithmWrapper decrypted = wrapper.decrypt(decoder);
        assertEquals(aesWrapper, decrypted);
    }
}
