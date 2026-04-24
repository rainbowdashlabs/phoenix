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
import dev.chojo.phoenix.crypto.processing.Decryptor;
import dev.chojo.phoenix.crypto.processing.Encryptor;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
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

class EncryptedSymAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void encrypt() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        AsymAlgorithmWrapper rsaWrapper =
                new AsymAlgorithmWrapper(rsa.getPublic(), "RSA/ECB/PKCS1Padding", Cipher.ENCRYPT_MODE);
        SymAlgorithmWrapper aesWrapper = cryptoService.randomAESKey();
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(rsaWrapper);
        EncryptedSymAlgorithmWrapper wrapper = EncryptedSymAlgorithmWrapper.encrypt(aesWrapper, encryptor);
        System.out.printf("Encrypted: %s%n", wrapper);
    }

    @Test
    void decrypt() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        String rsaCipher = "RSA/ECB/PKCS1Padding";
        AsymAlgorithmWrapper rsaPublic = new AsymAlgorithmWrapper(rsa.getPublic(), rsaCipher, Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper rsaPrivate = new AsymAlgorithmWrapper(rsa.getPrivate(), rsaCipher, Cipher.DECRYPT_MODE);
        SymAlgorithmWrapper aesWrapper = cryptoService.randomAESKey();
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(rsaPublic);
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(rsaPrivate);
        EncryptedSymAlgorithmWrapper wrapper = EncryptedSymAlgorithmWrapper.encrypt(aesWrapper, encryptor);
        SymAlgorithmWrapper decrypted = wrapper.decrypt(decryptor);
        assertEquals(aesWrapper, decrypted);
    }

    @Test
    void testCryptoExceptionInProcessor() throws Exception {
        AlgorithmWrapper<BytesProcessInput, BytesProcessResult> wrapper = mock(AlgorithmWrapper.class);
        when(wrapper.opMode()).thenReturn(Cipher.ENCRYPT_MODE);
        when(wrapper.process(any(BytesProcessInput.class))).thenThrow(new NoSuchAlgorithmException("failed"));
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(wrapper);
        assertThrows(CryptoException.class, () -> encryptor.process("data".getBytes()));
    }
}
