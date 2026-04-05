/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.processing.model.AESProcessInput;
import dev.chojo.crypto.processing.model.AESProcessResult;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;
import dev.chojo.crypto.processing.Encryptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptorTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    public void testEncode() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encryptor<AESProcessInput, AESProcessResult> encryptor = new Encryptor<>(aes);
        assertNotNull(encryptor.process("Hello".getBytes()));
    }

    @Test
    public void testProcessToString() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encryptor<AESProcessInput, AESProcessResult> encryptor = new Encryptor<>(aes);
        String encrypted = encryptor.processToString("Hello");
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());

        String encryptedFromBytes = encryptor.processToString("Hello".getBytes());
        assertNotNull(encryptedFromBytes);
        assertFalse(encryptedFromBytes.isEmpty());
    }

    @Test
    public void testWrapper() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encryptor<AESProcessInput, AESProcessResult> encryptor = new Encryptor<>(aes);
        assertEquals(aes, encryptor.wrapper());
    }

    @Test
    public void testProcessError() throws Exception {
        var wrapper = mock(dev.chojo.crypto.processing.wrapper.AlgorithmWrapper.class);
        when(wrapper.opMode()).thenReturn(Cipher.ENCRYPT_MODE);
        when(wrapper.process(any(BytesProcessInput.class))).thenThrow(new RuntimeException("test error"));
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(wrapper);
        assertThrows(RuntimeException.class, () -> encryptor.process("data".getBytes()));
    }

    @Test
    void testConstructorThrowsOnWrongMode() throws InvalidKeySpecException {
        AESAlgorithmWrapper aes = cryptoService.randomAESKey();
        // randomAESKey returns wrapper in ENCRYPT_MODE usually.
        // Let's create one with DECRYPT_MODE explicitly.
        AESAlgorithmWrapper aesDecrypt = new AESAlgorithmWrapper(aes.key(), aes.cipherName(), Cipher.DECRYPT_MODE);
        assertThrows(IllegalArgumentException.class, () -> new Encryptor<>(aesDecrypt));
    }

    @Test
    void testProcessRSA() throws Exception {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        String rsaCipher = "RSA/ECB/PKCS1Padding";
        RSAAlgorithmWrapper rsaWrapper = new RSAAlgorithmWrapper(keyPair.getPublic(), rsaCipher, Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(rsaWrapper);
        byte[] result = encryptor.process("test".getBytes()).bytes();
        assertNotNull(result);
    }
}
