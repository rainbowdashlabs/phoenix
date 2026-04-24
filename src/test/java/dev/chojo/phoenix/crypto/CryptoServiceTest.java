/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto;

import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.Root;
import dev.chojo.phoenix.crypto.processing.wrapper.SymAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
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
    void generateAsymetricAlgorithmPair() {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        assertNotNull(keyPair);
        assertEquals("RSA", keyPair.getPublic().getAlgorithm());
        // Verify key size by checking encoded length (not perfect but better than nothing)
        assertTrue(keyPair.getPublic().getEncoded().length > 200);
    }

    @Test
    void randomAESKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SymAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        assertNotNull(wrapper);
        assertNotNull(wrapper.key());
    }

    @Test
    void testConfigMethods() {
        assertEquals("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", cryptoService.asymmetricCipher());
        assertEquals("RSA", cryptoService.asymmetricAlgorithm());
        assertEquals(2048, cryptoService.asymmetricKeySize());
        assertEquals("AES/GCM/NoPadding", cryptoService.symmetricCipher());
        assertEquals(256, cryptoService.symmetricKeySize());
    }
}
