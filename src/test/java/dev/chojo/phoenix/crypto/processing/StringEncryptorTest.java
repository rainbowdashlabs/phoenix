/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing;

import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.Root;
import dev.chojo.phoenix.crypto.CryptoService;
import dev.chojo.phoenix.crypto.EncryptedContent;
import dev.chojo.phoenix.crypto.policy.KeyRotationPolicy;
import dev.chojo.phoenix.crypto.processing.Encryptor;
import dev.chojo.phoenix.crypto.processing.StringEncryptor;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
import dev.chojo.phoenix.crypto.processing.wrapper.AsymAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StringEncryptorTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testEncrypt() throws InvalidKeySpecException {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        AsymAlgorithmWrapper asymAlgorithmWrapper =
                new AsymAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding", javax.crypto.Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> rsa = new Encryptor<>(asymAlgorithmWrapper);

        String generatedString = "testContent";

        KeyRotationPolicy keyRotationPolicy =
                new KeyRotationPolicy(10000, () -> new Encryptor<>(cryptoService.randomAESKey()));
        EncryptedContent encrypted = new StringEncryptor(rsa, keyRotationPolicy).encrypt(generatedString);
        assertNotNull(encrypted.content());
        assertNotNull(encrypted.key());
        assertNotNull(encrypted.iv());
    }

    @Test
    void testEncryptWithoutIV() throws Exception {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        AsymAlgorithmWrapper asymAlgorithmWrapper =
                new AsymAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding", javax.crypto.Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> rsa = new Encryptor<>(asymAlgorithmWrapper);

        // Mocking rotation supplier to return a non-AES wrapper (which wouldn't have an IV)
        // Although the current implementation casts to AESAlgorithmWrapper, so this might fail.
        // Let's test what happens if it's NOT an AESProcessResult.

        Encryptor<BytesProcessInput, BytesProcessResult> rsaEnc = new Encryptor<>(asymAlgorithmWrapper);
        KeyRotationPolicy policy = new KeyRotationPolicy(1000, () -> (Encryptor) rsaEnc);

        StringEncryptor encoder = new StringEncryptor(rsa, policy);
        // This will likely throw ClassCastException at line 54 of StringEncoder.java
        assertThrows(ClassCastException.class, () -> encoder.encrypt("test"));
    }
}
