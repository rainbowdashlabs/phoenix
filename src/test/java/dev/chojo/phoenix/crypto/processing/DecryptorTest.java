/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing;

import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.Root;
import dev.chojo.phoenix.crypto.CryptoService;
import dev.chojo.phoenix.crypto.exceptions.CryptoException;
import dev.chojo.phoenix.crypto.processing.Decryptor;
import dev.chojo.phoenix.crypto.processing.Encryptor;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
import dev.chojo.phoenix.crypto.processing.model.SymProcessInput;
import dev.chojo.phoenix.crypto.processing.model.SymProcessResult;
import dev.chojo.phoenix.crypto.processing.wrapper.AlgorithmWrapper;
import dev.chojo.phoenix.crypto.processing.wrapper.AsymAlgorithmWrapper;
import dev.chojo.phoenix.crypto.processing.wrapper.SymAlgorithmWrapper;
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
                new Encryptor<>(new AsymAlgorithmWrapper(rsa.getPublic(), cipher, Cipher.ENCRYPT_MODE));
        byte[] encrypted = encryptor.process("Hello".getBytes()).bytes();
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor =
                new Decryptor<>(new AsymAlgorithmWrapper(rsa.getPrivate(), cipher, Cipher.DECRYPT_MODE));
        byte[] decoded = decryptor.process(encrypted).bytes();
        assertEquals("Hello", new String(decoded));
    }

    @Test
    void testProcessFromString() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        String cipher = "RSA/ECB/PKCS1Padding";
        var encryptWrapper = new AsymAlgorithmWrapper(rsa.getPublic(), cipher, Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(encryptWrapper);
        String encrypted = encryptor.processToString("Hello");

        var decryptWrapper = new AsymAlgorithmWrapper(rsa.getPrivate(), cipher, Cipher.DECRYPT_MODE);
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(decryptWrapper);
        byte[] decoded = decryptor.processFromString(encrypted);
        assertEquals("Hello", new String(decoded));

        String decodedString = decryptor.processFromStringToString(encrypted);
        assertEquals("Hello", decodedString);
    }

    @Test
    void testProcessError() throws Exception {
        var wrapper = mock(AlgorithmWrapper.class);
        when(wrapper.opMode()).thenReturn(Cipher.DECRYPT_MODE);
        when(wrapper.process(any(BytesProcessInput.class))).thenThrow(new RuntimeException("test error"));
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(wrapper);
        assertThrows(RuntimeException.class, () -> decryptor.process("data".getBytes()));
    }

    @Test
    void testAESDecryptionThrows() throws InvalidKeySpecException {
        SymAlgorithmWrapper aes = cryptoService.randomAESKey();
        SymAlgorithmWrapper aesDecrypt = new SymAlgorithmWrapper(aes.key(), aes.cipherName(), Cipher.DECRYPT_MODE);
        Decryptor<SymProcessInput, SymProcessResult> decryptor = new Decryptor<>(aesDecrypt);

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
                CryptoException.class,
                () -> decryptor.process(new BytesProcessInput(new byte[0])));
    }

    @Test
    void testConstructorThrowsOnWrongMode() throws InvalidKeySpecException {
        SymAlgorithmWrapper aes = cryptoService.randomAESKey();
        // aes is in ENCRYPT_MODE (0 in constructor call usually means ENCRYPT_MODE is not set yet,
        // but let's check what randomAESKey does)
        // Actually, let's just create one with ENCRYPT_MODE explicitly.
        SymAlgorithmWrapper aesEncrypt = new SymAlgorithmWrapper(aes.key(), aes.cipherName(), Cipher.ENCRYPT_MODE);
        assertThrows(IllegalArgumentException.class, () -> new Decryptor<>(aesEncrypt));
    }
}
