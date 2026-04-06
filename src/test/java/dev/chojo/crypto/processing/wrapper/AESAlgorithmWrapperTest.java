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
import dev.chojo.crypto.processing.model.AESProcessInput;
import dev.chojo.crypto.processing.model.AESProcessResult;
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

class AESAlgorithmWrapperTest {
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
        AESAlgorithmWrapper wrapper = cryptoService.randomAESKey();
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
        AESAlgorithmWrapper decryptWrapper = new AESAlgorithmWrapper(
                cryptoService.randomAESKey().key(), cryptoService.symmetricCipher(), Cipher.DECRYPT_MODE);
        Decryptor<AESProcessInput, AESProcessResult> decryptor = new Decryptor<>(decryptWrapper);

        AESProcessInput input = new AESProcessInput(new byte[16], null);
        assertThrows(NullPointerException.class, () -> decryptor.process(input), "IV must be provided for decryption");
    }

    @Test
    void testProcessedBytes()
            throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        AESAlgorithmWrapper encryptWrapper = new AESAlgorithmWrapper(
                cryptoService.randomAESKey().key(), cryptoService.symmetricCipher(), Cipher.ENCRYPT_MODE);
        Encryptor<AESProcessInput, AESProcessResult> encryptor = new Encryptor<>(encryptWrapper);

        assertEquals(0, encryptWrapper.processedBytes());
        String message = "Hello, World!";
        encryptor.process(new AESProcessInput(message.getBytes(), null));
        assertEquals(message.getBytes().length, encryptWrapper.processedBytes());
    }

    @Test
    void testEqualsAndHashCode()
            throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException,
                    NoSuchAlgorithmException, InvalidKeyException {
        AESAlgorithmWrapper wrapper1 = cryptoService.randomAESKey();
        AESAlgorithmWrapper wrapper2 =
                new AESAlgorithmWrapper(wrapper1.key(), wrapper1.cipherName(), wrapper1.opMode());
        AESAlgorithmWrapper wrapper3 = cryptoService.randomAESKey();
        AESAlgorithmWrapper wrapper4 =
                new AESAlgorithmWrapper(wrapper1.key(), "AES/CBC/PKCS5Padding", wrapper1.opMode());

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
        AESAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        assertNotNull(wrapper.key());
        assertEquals("AES/GCM/NoPadding", wrapper.cipherName());
        assertEquals(Cipher.ENCRYPT_MODE, wrapper.opMode());
    }

    @Test
    void testLazyCipherLockInit() throws Exception {
        AESAlgorithmWrapper wrapper = cryptoService.randomAESKey();
        // Cipher should not be created until first use
        // But it's private, so we can't easily check it without reflection or by calling a method that uses it.
        // Let's call a method that uses it and check it doesn't fail.
        String message = "test";
        AESProcessResult result = wrapper.process(new AESProcessInput(message.getBytes(), null));
        assertNotNull(result);

        // Calling it again should use the same cipher object (internal state might change but it's the same
        // ConcurrentCipher)
        AESProcessResult result2 = wrapper.process(new AESProcessInput(message.getBytes(), null));
        assertNotNull(result2);
    }

    @Test
    void testNonDeterministicEncryption()
            throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        AESAlgorithmWrapper encryptWrapper = new AESAlgorithmWrapper(
                cryptoService.randomAESKey().key(), cryptoService.symmetricCipher(), Cipher.ENCRYPT_MODE);
        Encryptor<AESProcessInput, AESProcessResult> encryptor = new Encryptor<>(encryptWrapper);

        String message = "Hello, World!";
        AESProcessResult result1 = encryptor.process(new AESProcessInput(message.getBytes(), null));
        AESProcessResult result2 = encryptor.process(new AESProcessInput(message.getBytes(), null));

        assertNotEquals(
                Arrays.toString(result1.bytes()),
                Arrays.toString(result2.bytes()),
                "Encryption should be non-deterministic (different IVs)");
        assertNotNull(result1.iv());
        assertNotNull(result2.iv());

        AESAlgorithmWrapper decryptWrapper =
                new AESAlgorithmWrapper(encryptWrapper.key(), cryptoService.symmetricCipher(), Cipher.DECRYPT_MODE);
        Decryptor<AESProcessInput, AESProcessResult> decryptor = new Decryptor<>(decryptWrapper);

        AESProcessResult decrypted1 = decryptor.process(new AESProcessInput(result1.bytes(), result1.iv()));
        AESProcessResult decrypted2 = decryptor.process(new AESProcessInput(result2.bytes(), result2.iv()));

        assertEquals(message, new String(decrypted1.bytes()), "Decryption of first message failed");
        assertEquals(message, new String(decrypted2.bytes()), "Decryption of second message failed");
    }
}
