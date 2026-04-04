/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CryptoServiceTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void generateKeyPair() {
        KeyPair keyPair = cryptoService.generateKeyPair();
        assertNotNull(keyPair);
    }

    @Test
    void encrypt() {
        KeyPair keyPair = cryptoService.generateKeyPair();
        byte[] testStrings = cryptoService.encrypt("Test String", keyPair.getPublic());
        assertNotEquals("Test String".getBytes(), testStrings);
    }

    @Test
    void decrypt() {
        KeyPair keyPair = cryptoService.generateKeyPair();
        byte[] bytes = cryptoService.encrypt("Test String", keyPair.getPublic());
        String decrypted = cryptoService.decryptString(bytes, keyPair.getPrivate());
        assertEquals("Test String", decrypted);
    }

    @Test
    void serializePublicKey() {
        Key key = cryptoService.generateKeyPair().getPublic();
        String serializedKey = cryptoService.serializeKey(key);
        System.out.println(serializedKey);
        assertNotNull(serializedKey);
    }

    @Test
    void serializePrivateKey() {
        Key key = cryptoService.generateKeyPair().getPublic();
        String serializedKey = cryptoService.serializeKey(key);
        System.out.println(serializedKey);
        assertNotNull(serializedKey);
    }

    @Test
    void deserializePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Key original = cryptoService.generateKeyPair().getPublic();
        String serializedKey = cryptoService.serializeKey(original);
        PublicKey key = cryptoService.deserializePublicKey(serializedKey);
        assertEquals(original.getAlgorithm(), key.getAlgorithm());
        assertEquals(original.getFormat(), key.getFormat());
        assertArrayEquals(original.getEncoded(), key.getEncoded());
    }

    @Test
    void deserializePrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Key original = cryptoService.generateKeyPair().getPrivate();
        String serializedKey = cryptoService.serializeKey(original);
        PrivateKey key = cryptoService.deserializePrivateKey(serializedKey);
        assertEquals(original.getAlgorithm(), key.getAlgorithm());
        assertEquals(original.getFormat(), key.getFormat());
        assertArrayEquals(original.getEncoded(), key.getEncoded());
    }
}
