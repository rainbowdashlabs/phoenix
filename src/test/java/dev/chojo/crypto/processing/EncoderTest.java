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

class EncoderTest {
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
        Encoder encoder = new Encoder(aes);
        assertNotNull(encoder.process("Hello".getBytes()));
    }

    @Test
    public void testProcessToString() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encoder encoder = new Encoder(aes);
        String encrypted = encoder.processToString("Hello");
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());

        String encryptedFromBytes = encoder.processToString("Hello".getBytes());
        assertNotNull(encryptedFromBytes);
        assertFalse(encryptedFromBytes.isEmpty());
    }

    @Test
    public void testWrapper() throws InvalidKeySpecException {
        var aes = cryptoService.randomAESKey();
        Encoder encoder = new Encoder(aes);
        assertEquals(aes, encoder.wrapper());
    }

    @Test
    public void testProcessError() throws Exception {
        var wrapper = mock(dev.chojo.crypto.processing.wrapper.AlgorithmWrapper.class);
        when(wrapper.process(any(byte[].class), anyInt())).thenThrow(new RuntimeException("test error"));
        Encoder encoder = new Encoder(wrapper);
        assertThrows(RuntimeException.class, () -> encoder.process("data".getBytes()));
    }
}
