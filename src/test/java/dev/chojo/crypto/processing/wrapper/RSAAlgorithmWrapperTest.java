/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RSAAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testEqualsAndHashCode() {
        KeyPair keyPair1 = cryptoService.generateRSAKeyPair();
        KeyPair keyPair2 = cryptoService.generateRSAKeyPair();

        RSAAlgorithmWrapper wrapper1 = new RSAAlgorithmWrapper(keyPair1.getPublic(), "RSA/ECB/PKCS1Padding");
        RSAAlgorithmWrapper wrapper2 = new RSAAlgorithmWrapper(keyPair1.getPublic(), "RSA/ECB/PKCS1Padding");
        RSAAlgorithmWrapper wrapper3 = new RSAAlgorithmWrapper(keyPair2.getPublic(), "RSA/ECB/PKCS1Padding");
        RSAAlgorithmWrapper wrapper4 =
                new RSAAlgorithmWrapper(keyPair1.getPublic(), "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
        assertNotEquals(wrapper1, wrapper3);
        assertNotEquals(wrapper1, wrapper4);
        assertNotEquals(wrapper1, null);
        assertNotEquals(wrapper1, "string");
    }

    @Test
    void testProcess() throws Exception {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        RSAAlgorithmWrapper wrapper = new RSAAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding");
        byte[] data = "test".getBytes();
        byte[] encrypted = wrapper.process(data, javax.crypto.Cipher.ENCRYPT_MODE);
        assertNotNull(encrypted);
        assertNotEquals(data, encrypted);
    }

    @Test
    void testGetters() {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        RSAAlgorithmWrapper wrapper = new RSAAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding");
        assertEquals(keyPair.getPublic(), wrapper.key());
        assertEquals("RSA/ECB/PKCS1Padding", wrapper.cipher());
    }
}
