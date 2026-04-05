/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.AsymmetricKey;
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
    void generateRSAKeyPair() {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        assertNotNull(keyPair);
        assertEquals("RSA", keyPair.getPublic().getAlgorithm());
        // Verify key size by checking encoded length (not perfect but better than nothing)
        assertTrue(keyPair.getPublic().getEncoded().length > 200);
    }

    @Test
    void serializePublicKey() {
        PublicKey key = (PublicKey) cryptoService.generateRSAKeyPair().getPublic();
        String serializedKey = cryptoService.serializeKey(key);
        assertTrue(serializedKey.startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(serializedKey.endsWith("-----END PUBLIC KEY-----"));
    }

    @Test
    void serializePrivateKey() {
        PrivateKey key = (PrivateKey) cryptoService.generateRSAKeyPair().getPrivate();
        String serializedKey = cryptoService.serializeKey(key);
        assertTrue(serializedKey.startsWith("-----BEGIN PRIVATE KEY-----"));
        assertTrue(serializedKey.endsWith("-----END PRIVATE KEY-----"));
    }

    @Test
    void serializeUnknownKey() {
        AsymmetricKey key = mock(AsymmetricKey.class);
        when(key.getEncoded()).thenReturn(new byte[0]);
        String serializedKey = cryptoService.serializeKey(key);
        assertNotNull(serializedKey);
        assertFalse(serializedKey.contains("BEGIN"));
    }

    @Test
    void deserializePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        AsymmetricKey original = cryptoService.generateRSAKeyPair().getPublic();
        String serializedKey = cryptoService.serializeKey(original);
        PublicKey key = cryptoService.deserializePublicKey(serializedKey);
        assertEquals(original.getAlgorithm(), key.getAlgorithm());
        assertEquals(original.getFormat(), key.getFormat());
        assertArrayEquals(original.getEncoded(), key.getEncoded());

        // Test with different format (extra whitespace/newlines)
        String messyKey = "\n  " + serializedKey.replace("\n", "\n  ") + " \n";
        PublicKey key2 = cryptoService.deserializePublicKey(messyKey);
        assertArrayEquals(original.getEncoded(), key2.getEncoded());
    }

    @Test
    void deserializePrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        AsymmetricKey original = cryptoService.generateRSAKeyPair().getPrivate();
        String serializedKey = cryptoService.serializeKey(original);
        PrivateKey key = cryptoService.deserializePrivateKey(serializedKey);
        assertEquals(original.getAlgorithm(), key.getAlgorithm());
        assertEquals(original.getFormat(), key.getFormat());
        assertArrayEquals(original.getEncoded(), key.getEncoded());

        // Test with different format (extra whitespace/newlines)
        String messyKey = "\n  " + serializedKey.replace("\n", "\n  ") + " \n";
        PrivateKey key2 = cryptoService.deserializePrivateKey(messyKey);
        assertArrayEquals(original.getEncoded(), key2.getEncoded());
    }

    @Test
    void randomAESKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        AESAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        assertNotNull(wrapper);
        assertNotNull(wrapper.key());
        assertEquals(12, wrapper.iv().length);

        // Verify IV is random by generating another one
        AESAlgorithmWrapper wrapper2 = cryptoService.randomAESKey();
        assertFalse(java.util.Arrays.equals(wrapper.iv(), wrapper2.iv()));
    }

    @Test
    void generateAESKey() throws InvalidKeySpecException {
        char[] password = "password".toCharArray();
        byte[] salt = new byte[16];
        var key = cryptoService.generateAESKey(password, salt);
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }

    @Test
    void testConfigMethods() {
        assertEquals("RSA/ECB/PKCS1Padding", cryptoService.rsaCipher());
        assertEquals("RSA", cryptoService.rsaKey());
        assertEquals(2048, cryptoService.rsaKeySize());
        assertEquals("AES/GCM/NoPadding", cryptoService.aesCipher());
        assertEquals(256, cryptoService.aesKeySize());
        assertEquals(65536, cryptoService.aesIterations());
    }
}
