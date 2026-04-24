/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.serialization;

import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.Root;
import dev.chojo.phoenix.crypto.CryptoService;
import dev.chojo.phoenix.crypto.exceptions.CryptoException;
import dev.chojo.phoenix.crypto.processing.wrapper.AsymAlgorithmWrapper;
import dev.chojo.phoenix.crypto.serialization.PlainAsymAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlainAsymAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testWrapUnwrapPublic() {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        String cipher = "RSA/ECB/PKCS1Padding";
        AsymAlgorithmWrapper rsaWrapper = new AsymAlgorithmWrapper(keyPair.getPublic(), cipher);

        PlainAsymAlgorithmWrapper encrypted = PlainAsymAlgorithmWrapper.wrap(rsaWrapper);
        assertTrue(encrypted.key().contains("-----BEGIN PUBLIC KEY-----"));

        AsymAlgorithmWrapper decrypted = encrypted.unwrap();
        assertEquals(rsaWrapper, decrypted);
    }

    @Test
    void testWrapUnwrapPrivate() {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        String cipher = "RSA/ECB/PKCS1Padding";
        AsymAlgorithmWrapper rsaWrapper = new AsymAlgorithmWrapper(keyPair.getPrivate(), cipher);

        PlainAsymAlgorithmWrapper encrypted = PlainAsymAlgorithmWrapper.wrap(rsaWrapper);
        assertTrue(encrypted.key().contains("-----BEGIN PRIVATE KEY-----"));

        AsymAlgorithmWrapper decrypted = encrypted.unwrap();
        assertEquals(rsaWrapper, decrypted);
    }

    @Test
    void testUnsupportedKey() {
        java.security.Key key = mock(java.security.Key.class);
        when(key.getEncoded()).thenReturn(new byte[0]);
        AsymAlgorithmWrapper rsaWrapper = new AsymAlgorithmWrapper(key, "RSA/ECB/PKCS1Padding");
        assertThrows(CryptoException.class, () -> PlainAsymAlgorithmWrapper.wrap(rsaWrapper));
    }

    @Test
    void testInvalidFormat() {
        PlainAsymAlgorithmWrapper wrapper =
                new PlainAsymAlgorithmWrapper("-----BEGIN UNKNOWN-----", "RSA/ECB/PKCS1Padding");
        CryptoException exception = assertThrows(CryptoException.class, wrapper::unwrap);
        assertEquals("Invalid RSA key format", exception.getMessage());
    }

    @Test
    void testInvalidBase64() {
        PlainAsymAlgorithmWrapper wrapper = new PlainAsymAlgorithmWrapper(
                "-----BEGIN PUBLIC KEY-----\n!@#$\n-----END PUBLIC KEY-----", "RSA/ECB/PKCS1Padding");
        CryptoException exception = assertThrows(CryptoException.class, wrapper::unwrap);
        assertEquals("Invalid base64 in RSA key", exception.getMessage());
    }

    @Test
    void testDecryptFailure() {
        // Correct base64 but not a valid RSA key
        PlainAsymAlgorithmWrapper wrapper = new PlainAsymAlgorithmWrapper(
                "-----BEGIN PUBLIC KEY-----\nSGVsbG8gd29ybGQ=\n-----END PUBLIC KEY-----", "RSA/ECB/PKCS1Padding");
        CryptoException exception = assertThrows(CryptoException.class, wrapper::unwrap);
        assertTrue(exception.getMessage().contains("Could not decrypt RSA wrapper"));
    }
}
