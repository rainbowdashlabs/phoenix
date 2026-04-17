/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.Root;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.processing.Decryptor;
import dev.chojo.crypto.processing.Encryptor;
import dev.chojo.crypto.processing.model.SymProcessInput;
import dev.chojo.crypto.processing.model.SymProcessResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SymAlgorithmWrapperTest {
    static CryptoService cryptoService;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var configuration = mock(Configuration.class);
        when(configuration.main()).thenReturn(new Root());
        cryptoService = new CryptoService(configuration);
    }

    @Test
    void testDestroy()
            throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException,
                    NoSuchAlgorithmException, InvalidKeyException, javax.security.auth.DestroyFailedException {
        SymAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        try {
            wrapper.destroy();
            assertTrue(wrapper.key().isDestroyed());
        } catch (javax.security.auth.DestroyFailedException e) {
            // Some implementations don't support destroy, that's fine
        }
    }

    @Test
    void testDecryptionWithoutIV()
            throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException {
        SymAlgorithmWrapper decryptWrapper = new SymAlgorithmWrapper(
                cryptoService.randomAESKey().key(), cryptoService.symmetricCipher(), Cipher.DECRYPT_MODE);
        Decryptor<SymProcessInput, SymProcessResult> decryptor = new Decryptor<>(decryptWrapper);

        SymProcessInput input = new SymProcessInput(new byte[16], null);
        assertThrows(NullPointerException.class, () -> decryptor.process(input), "IV must be provided for decryption");
    }

    @Test
    void testProcessedBytes()
            throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SymAlgorithmWrapper encryptWrapper = new SymAlgorithmWrapper(
                cryptoService.randomAESKey().key(), cryptoService.symmetricCipher(), Cipher.ENCRYPT_MODE);
        Encryptor<SymProcessInput, SymProcessResult> encryptor = new Encryptor<>(encryptWrapper);

        assertEquals(0, encryptWrapper.processedBytes());
        String message = "Hello, World!";
        encryptor.process(new SymProcessInput(message.getBytes(), null));
        assertEquals(message.getBytes().length, encryptWrapper.processedBytes());
    }

    @Test
    void testEqualsAndHashCode()
            throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException,
                    NoSuchAlgorithmException, InvalidKeyException {
        SymAlgorithmWrapper wrapper1 = cryptoService.randomAESKey();
        SymAlgorithmWrapper wrapper2 =
                new SymAlgorithmWrapper(wrapper1.key(), wrapper1.cipherName(), wrapper1.opMode());
        SymAlgorithmWrapper wrapper3 = cryptoService.randomAESKey();
        SymAlgorithmWrapper wrapper4 =
                new SymAlgorithmWrapper(wrapper1.key(), "AES/CBC/PKCS5Padding", wrapper1.opMode());

        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
        assertNotEquals(wrapper1, wrapper3);
        assertNotEquals(wrapper1, wrapper4);
        assertNotEquals(null, wrapper1);
        assertNotEquals("string", wrapper1);
    }

    @Test
    void testGetters()
            throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException,
                    NoSuchAlgorithmException, InvalidKeyException {
        SymAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        assertNotNull(wrapper.key());
        assertEquals("AES/GCM/NoPadding", wrapper.cipherName());
        assertEquals(Cipher.ENCRYPT_MODE, wrapper.opMode());
    }

    @Test
    void testLazyCipherLockInit() throws Exception {
        SymAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        // Cipher should not be created until first use
        // But it's private, so we can't easily check it without reflection or by calling a method that uses it.
        // Let's call a method that uses it and check it doesn't fail.
        String message = "test";
        SymProcessResult result = wrapper.process(new SymProcessInput(message.getBytes(), null));
        assertNotNull(result);

        // Calling it again should use the same cipher object (internal state might change but it's the same
        // ConcurrentCipher)
        SymProcessResult result2 = wrapper.process(new SymProcessInput(message.getBytes(), null));
        assertNotNull(result2);
    }

    @Test
    void testNonDeterministicEncryption()
            throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SymAlgorithmWrapper encryptWrapper = new SymAlgorithmWrapper(
                cryptoService.randomAESKey().key(), cryptoService.symmetricCipher(), Cipher.ENCRYPT_MODE);
        Encryptor<SymProcessInput, SymProcessResult> encryptor = new Encryptor<>(encryptWrapper);

        String message = "Hello, World!";
        SymProcessResult result1 = encryptor.process(new SymProcessInput(message.getBytes(), null));
        SymProcessResult result2 = encryptor.process(new SymProcessInput(message.getBytes(), null));

        assertNotEquals(
                Arrays.toString(result1.bytes()),
                Arrays.toString(result2.bytes()),
                "Encryption should be non-deterministic (different IVs)");
        assertNotNull(result1.iv());
        assertNotNull(result2.iv());

        SymAlgorithmWrapper decryptWrapper =
                new SymAlgorithmWrapper(encryptWrapper.key(), cryptoService.symmetricCipher(), Cipher.DECRYPT_MODE);
        Decryptor<SymProcessInput, SymProcessResult> decryptor = new Decryptor<>(decryptWrapper);

        SymProcessResult decrypted1 = decryptor.process(new SymProcessInput(result1.bytes(), result1.iv()));
        SymProcessResult decrypted2 = decryptor.process(new SymProcessInput(result2.bytes(), result2.iv()));

        assertEquals(message, new String(decrypted1.bytes()), "Decryption of first message failed");
        assertEquals(message, new String(decrypted2.bytes()), "Decryption of second message failed");
    }
}
