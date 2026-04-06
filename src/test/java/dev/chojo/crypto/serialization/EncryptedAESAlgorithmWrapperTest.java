/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.serialization;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.exceptions.CryptoException;
import dev.chojo.crypto.processing.Decryptor;
import dev.chojo.crypto.processing.Encryptor;
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

class EncryptedAESAlgorithmWrapperTest {
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
        RSAAlgorithmWrapper rsaWrapper =
                new RSAAlgorithmWrapper(rsa.getPublic(), "RSA/ECB/PKCS1Padding", Cipher.ENCRYPT_MODE);
        AESAlgorithmWrapper aesWrapper = cryptoService.randomAESKey();
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(rsaWrapper);
        EncryptedAESAlgorithmWrapper wrapper = EncryptedAESAlgorithmWrapper.encrypt(aesWrapper, encryptor);
        System.out.printf("Encrypted: %s%n", wrapper);
    }

    @Test
    void decrypt() throws InvalidKeySpecException {
        KeyPair rsa = cryptoService.generateRSAKeyPair();
        String rsaCipher = "RSA/ECB/PKCS1Padding";
        RSAAlgorithmWrapper rsaPublic = new RSAAlgorithmWrapper(rsa.getPublic(), rsaCipher, Cipher.ENCRYPT_MODE);
        RSAAlgorithmWrapper rsaPrivate = new RSAAlgorithmWrapper(rsa.getPrivate(), rsaCipher, Cipher.DECRYPT_MODE);
        AESAlgorithmWrapper aesWrapper = cryptoService.randomAESKey();
        Encryptor<BytesProcessInput, BytesProcessResult> encryptor = new Encryptor<>(rsaPublic);
        Decryptor<BytesProcessInput, BytesProcessResult> decryptor = new Decryptor<>(rsaPrivate);
        EncryptedAESAlgorithmWrapper wrapper = EncryptedAESAlgorithmWrapper.encrypt(aesWrapper, encryptor);
        AESAlgorithmWrapper decrypted = wrapper.decrypt(decryptor);
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
