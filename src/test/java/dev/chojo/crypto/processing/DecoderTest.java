/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DecoderTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testDecode() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encoder encoder = new Encoder(aes);
        byte[] encrypted = encoder.process("Hello".getBytes());
        Decoder decoder = new Decoder(aes);
        byte[] decoded = decoder.process(encrypted);
        assertEquals("Hello", new String(decoded));
    }

    @Test
    void testProcessFromString() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encoder encoder = new Encoder(aes);
        String encrypted = encoder.processToString("Hello");

        Decoder decoder = new Decoder(aes);
        byte[] decoded = decoder.processFromString(encrypted);
        assertEquals("Hello", new String(decoded));

        String decodedString = decoder.processFromStringToString(encrypted);
        assertEquals("Hello", decodedString);
    }

    @Test
    void testProcessError() throws Exception {
        var wrapper = mock(dev.chojo.crypto.processing.wrapper.AlgorithmWrapper.class);
        when(wrapper.process(any(byte[].class), anyInt())).thenThrow(new RuntimeException("test error"));
        Decoder decoder = new Decoder(wrapper);
        assertThrows(RuntimeException.class, () -> decoder.process("data".getBytes()));
    }
}
