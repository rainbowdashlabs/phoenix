/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing.wrapper;

import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.Root;
import dev.chojo.phoenix.crypto.CryptoService;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
import dev.chojo.phoenix.crypto.processing.wrapper.AsymAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AsymAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testFullCycle() throws Exception {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        String cipher = "RSA/ECB/PKCS1Padding";

        AsymAlgorithmWrapper encryptWrapper =
                new AsymAlgorithmWrapper(keyPair.getPublic(), cipher, javax.crypto.Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper decryptWrapper =
                new AsymAlgorithmWrapper(keyPair.getPrivate(), cipher, javax.crypto.Cipher.DECRYPT_MODE);

        byte[] data = "Hello RSA".getBytes();
        BytesProcessResult encrypted = encryptWrapper.process(new BytesProcessInput(data));
        BytesProcessResult decrypted = decryptWrapper.process(new BytesProcessInput(encrypted.bytes()));

        assertArrayEquals(data, decrypted.bytes());
    }

    @Test
    void testEqualsAndHashCode() {
        KeyPair keyPair1 = cryptoService.generateRSAKeyPair();
        KeyPair keyPair2 = cryptoService.generateRSAKeyPair();
        String cipher1 = "RSA/ECB/PKCS1Padding";
        String cipher2 = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

        AsymAlgorithmWrapper wrapper1 =
                new AsymAlgorithmWrapper(keyPair1.getPublic(), cipher1, javax.crypto.Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper wrapper2 =
                new AsymAlgorithmWrapper(keyPair1.getPublic(), cipher1, javax.crypto.Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper wrapper3 =
                new AsymAlgorithmWrapper(keyPair2.getPublic(), cipher1, javax.crypto.Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper wrapper4 =
                new AsymAlgorithmWrapper(keyPair1.getPublic(), cipher2, javax.crypto.Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper wrapper5 =
                new AsymAlgorithmWrapper(keyPair1.getPublic(), cipher1, javax.crypto.Cipher.DECRYPT_MODE);

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
        assertNotEquals(wrapper1, wrapper3);
        assertNotEquals(wrapper1, wrapper4);
        // opMode is not included in equals/hashCode of RSAAlgorithmWrapper based on its implementation
        assertEquals(wrapper1, wrapper5);
        assertNotEquals(wrapper1, null);
        assertNotEquals(wrapper1, "string");
    }

    @Test
    void testProcess() throws Exception {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        AsymAlgorithmWrapper wrapper =
                new AsymAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding", javax.crypto.Cipher.ENCRYPT_MODE);
        byte[] data = "test".getBytes();
        BytesProcessResult result = wrapper.process(new BytesProcessInput(data));
        byte[] encrypted = result.bytes();
        assertNotNull(encrypted);
        assertNotEquals(data, encrypted);
    }

    @Test
    void testGetters() {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        AsymAlgorithmWrapper wrapper =
                new AsymAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding", javax.crypto.Cipher.ENCRYPT_MODE);
        assertEquals(keyPair.getPublic(), wrapper.key());
        assertEquals("RSA/ECB/PKCS1Padding", wrapper.cipherName());
        assertEquals(javax.crypto.Cipher.ENCRYPT_MODE, wrapper.opMode());
        assertEquals(0, wrapper.processedBytes());
    }
}
