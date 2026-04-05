/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AESAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testEqualsAndHashCode() throws InvalidKeySpecException {
        AESAlgorithmWrapper wrapper1 = cryptoService.randomAESKey();
        AESAlgorithmWrapper wrapper2 = new AESAlgorithmWrapper(wrapper1.key(), wrapper1.iv(), wrapper1.cipher());
        AESAlgorithmWrapper wrapper3 = cryptoService.randomAESKey();

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
        assertNotEquals(wrapper1, wrapper3);
        assertNotEquals(null, wrapper1);
        assertNotEquals("string", wrapper1);
    }

    @Test
    void testGetters() throws InvalidKeySpecException {
        AESAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        assertNotNull(wrapper.key());
        assertNotNull(wrapper.iv());
        assertEquals("AES/GCM/NoPadding", wrapper.cipher());
    }

    @Test
    void testGcmParameterSpec() throws InvalidKeySpecException {
        AESAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        var spec1 = wrapper.gcmParameterSpec();
        var spec2 = wrapper.gcmParameterSpec();
        assertSame(spec1, spec2);
        assertEquals(128, spec1.getTLen());
        assertArrayEquals(wrapper.iv(), spec1.getIV());
    }
}
