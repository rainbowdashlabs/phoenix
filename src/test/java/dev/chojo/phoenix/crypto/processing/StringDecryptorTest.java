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
import dev.chojo.phoenix.crypto.processing.Decryptor;
import dev.chojo.phoenix.crypto.processing.Encryptor;
import dev.chojo.phoenix.crypto.processing.StringDecrypter;
import dev.chojo.phoenix.crypto.processing.StringEncryptor;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
import dev.chojo.phoenix.crypto.processing.wrapper.AsymAlgorithmWrapper;
import dev.chojo.phoenix.crypto.processing.wrapper.SymAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StringDecryptorTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testDecode() throws InvalidKeySpecException {
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        String rsaCipher = "RSA/ECB/PKCS1Padding";
        AsymAlgorithmWrapper publicRSA =
                new AsymAlgorithmWrapper(keyPair.getPublic(), rsaCipher, javax.crypto.Cipher.ENCRYPT_MODE);
        AsymAlgorithmWrapper privateRSA =
                new AsymAlgorithmWrapper(keyPair.getPrivate(), rsaCipher, javax.crypto.Cipher.DECRYPT_MODE);
        SymAlgorithmWrapper symAlgorithmWrapper = cryptoService.randomAESKey();
        Encryptor<BytesProcessInput, BytesProcessResult> rsa = new Encryptor<>(publicRSA);
        String generatedString = new Random()
                .ints('0', 'z' + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(4000)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        KeyRotationPolicy keyRotationPolicy =
                new KeyRotationPolicy(10000, () -> new Encryptor<>(cryptoService.randomAESKey()));
        EncryptedContent encrypted = new StringEncryptor(rsa, keyRotationPolicy).encrypt(generatedString);
        String decode = new StringDecrypter(new Decryptor<>(privateRSA)).decode(encrypted);
        assertEquals(generatedString, decode);
    }
}
