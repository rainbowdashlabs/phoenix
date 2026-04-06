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
import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DecryptorTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testDecode() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        String cipher = "RSA/ECB/PKCS1Padding";
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor =
                new Encryptor<>(new RSAAlgorithmWrapper(rsa.getPublic(), cipher, Cipher.ENCRYPT_MODE));
        byte[] encrypted = encryptor.process("Hello".getBytes()).bytes();
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor =
                new Decryptor<>(new RSAAlgorithmWrapper(rsa.getPrivate(), cipher, Cipher.DECRYPT_MODE));
        byte[] decoded = decryptor.process(encrypted).bytes();
        assertEquals("Hello", new String(decoded));
    }

    @Test
    void testProcessFromString() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        String cipher = "RSA/ECB/PKCS1Padding";
        var encryptWrapper = new RSAAlgorithmWrapper(rsa.getPublic(), cipher, Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(encryptWrapper);
        String encrypted = encryptor.processToString("Hello");

        var decryptWrapper = new RSAAlgorithmWrapper(rsa.getPrivate(), cipher, Cipher.DECRYPT_MODE);
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(decryptWrapper);
        byte[] decoded = decryptor.processFromString(encrypted);
        assertEquals("Hello", new String(decoded));

        String decodedString = decryptor.processFromStringToString(encrypted);
        assertEquals("Hello", decodedString);
    }

    @Test
    void testProcessError() throws Exception {
        var wrapper = mock(dev.chojo.crypto.processing.wrapper.AlgorithmWrapper.class);
        when(wrapper.opMode()).thenReturn(Cipher.DECRYPT_MODE);
        when(wrapper.process(any(BytesProcessInput.class))).thenThrow(new RuntimeException("test error"));
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(wrapper);
        assertThrows(RuntimeException.class, () -> decryptor.process("data".getBytes()));
    }

    @Test
    void testAESDecryptionThrows() throws InvalidKeySpecException {
        AESAlgorithmWrapper aes = cryptoService.randomAESKey();
        AESAlgorithmWrapper aesDecrypt = new AESAlgorithmWrapper(aes.key(), aes.cipherName(), Cipher.DECRYPT_MODE);
        Decryptor<AESProcessInput, AESProcessResult> decryptor = new Decryptor<>(aesDecrypt);

        assertThrows(UnsupportedOperationException.class, () -> decryptor.process("data".getBytes()));
    }

    @Test
    void testProcessWithNullResultThrows() throws Exception {
        @SuppressWarnings("unchecked")
        AlgorithmWrapper<BytesProcessInput, BytesProcessResult> wrapper = mock(AlgorithmWrapper.class);
        when(wrapper.opMode()).thenReturn(Cipher.DECRYPT_MODE);
        when(wrapper.process(any(BytesProcessInput.class))).thenReturn(null);

        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(wrapper);
        assertThrows(
                dev.chojo.crypto.exceptions.CryptoException.class,
                () -> decryptor.process(new BytesProcessInput(new byte[0])));
    }

    @Test
    void testConstructorThrowsOnWrongMode() throws InvalidKeySpecException {
        AESAlgorithmWrapper aes = cryptoService.randomAESKey();
        // aes is in ENCRYPT_MODE (0 in constructor call usually means ENCRYPT_MODE is not set yet,
        // but let's check what randomAESKey does)
        // Actually, let's just create one with ENCRYPT_MODE explicitly.
        AESAlgorithmWrapper aesEncrypt = new AESAlgorithmWrapper(aes.key(), aes.cipherName(), Cipher.ENCRYPT_MODE);
        assertThrows(IllegalArgumentException.class, () -> new Decryptor<>(aesEncrypt));
    }
}
