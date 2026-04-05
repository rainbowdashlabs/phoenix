/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.policy.KeyRotationPolicy;
import dev.chojo.crypto.processing.model.AESProcessResult;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void testEncode() throws InvalidKeySpecException {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        RSAAlgorithmWrapper rsaAlgorithmWrapper =
                new RSAAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding", javax.crypto.Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> rsa = new Encryptor<>(rsaAlgorithmWrapper);

        String generatedString = "testContent";

        KeyRotationPolicy keyRotationPolicy =
                new KeyRotationPolicy(10000, () -> new Encryptor<>(cryptoService.randomAESKey()));
        EncryptedContent encrypted = new StringEncoder(rsa, keyRotationPolicy).encode(generatedString);
        assertNotNull(encrypted.content());
        assertNotNull(encrypted.key());
        assertNotNull(encrypted.iv());
    }

    @Test
    void testEncodeWithoutIV() throws Exception {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        RSAAlgorithmWrapper rsaAlgorithmWrapper =
                new RSAAlgorithmWrapper(keyPair.getPublic(), "RSA/ECB/PKCS1Padding", javax.crypto.Cipher.ENCRYPT_MODE);
        Encryptor<BytesProcessInput, BytesProcessResult> rsa = new Encryptor<>(rsaAlgorithmWrapper);

        // Mocking rotation supplier to return a non-AES wrapper (which wouldn't have an IV)
        // Although the current implementation casts to AESAlgorithmWrapper, so this might fail.
        // Let's test what happens if it's NOT an AESProcessResult.
        
        Encryptor<BytesProcessInput, BytesProcessResult> rsaEnc = new Encryptor<>(rsaAlgorithmWrapper);
        KeyRotationPolicy policy = new KeyRotationPolicy(1000, () -> (Encryptor) rsaEnc);
        
        StringEncoder encoder = new StringEncoder(rsa, policy);
        // This will likely throw ClassCastException at line 54 of StringEncoder.java
        assertThrows(ClassCastException.class, () -> encoder.encode("test"));
    }
}
