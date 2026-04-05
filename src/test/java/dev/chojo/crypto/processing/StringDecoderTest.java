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
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StringDecoderTest {
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
        RSAAlgorithmWrapper publicRSA = new RSAAlgorithmWrapper(keyPair.getPublic());
        RSAAlgorithmWrapper privateRSA = new RSAAlgorithmWrapper(keyPair.getPrivate());
        AESAlgorithmWrapper aesAlgorithmWrapper = cryptoService.randomAESKey();
        Encoder rsa = new Encoder(publicRSA);
        Encoder aes = new Encoder(aesAlgorithmWrapper);
        String generatedString = new Random()
                .ints('0', 'z' + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(4000)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        EncryptedContent encrypted = new StringEncoder(rsa, aes).encode(generatedString);
        String decode = new StringDecoder(new Decoder(privateRSA)).decode(encrypted);
        assertEquals(generatedString, decode);
    }
}
