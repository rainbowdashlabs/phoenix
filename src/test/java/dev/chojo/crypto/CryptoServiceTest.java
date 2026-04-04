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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

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
}
