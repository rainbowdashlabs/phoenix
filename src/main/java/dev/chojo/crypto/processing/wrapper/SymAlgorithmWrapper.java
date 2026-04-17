/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import dev.chojo.crypto.processing.model.SymProcessInput;
import dev.chojo.crypto.processing.model.SymProcessResult;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.security.auth.DestroyFailedException;

public class SymAlgorithmWrapper extends AlgorithmWrapper<SymProcessInput, SymProcessResult> {
    private static final SecureRandom secureRandom = new SecureRandom();
    /// The AES key.
    private final SecretKey key;

    private int processedBytes = 0;

    public SymAlgorithmWrapper(SecretKey key, String cipher) {
        this(key, cipher, 0);
    }

    public SymAlgorithmWrapper(SecretKey key, String cipher, int opMode) {
        super(cipher, opMode);
        this.key = key;
    }

    @Override
    public int processedBytes() {
        return processedBytes;
    }

    private GCMParameterSpec newNonce() {
        var iv = new byte[12];
        secureRandom.nextBytes(iv);
        return nonce(iv);
    }

    private GCMParameterSpec nonce(byte[] iv) {
        return new GCMParameterSpec(128, iv);
    }

    @Override
    public SymProcessResult process(SymProcessInput input)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        try (var lock = cipherLock()) {
            Cipher cipher = lock.cipher();
            if (opMode == Cipher.ENCRYPT_MODE) {
                GCMParameterSpec nonce = newNonce();
                cipher.init(opMode, key, nonce);
                processedBytes += input.bytes().length;
                byte[] encrypted = cipher.doFinal(input.bytes());
                return new SymProcessResult(encrypted, nonce.getIV());
            } else {
                byte[] usedIv = input.iv();
                Objects.requireNonNull(usedIv, "IV must be provided for decryption");
                cipher.init(opMode, key, nonce(usedIv));
                byte[] decrypted = cipher.doFinal(input.bytes());
                return new SymProcessResult(decrypted, null);
            }
        }
    }

    public SecretKey key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SymAlgorithmWrapper that = (SymAlgorithmWrapper) o;
        return Objects.equals(key, that.key) && Objects.equals(cipherName, that.cipherName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, cipherName);
    }

    @Override
    public void destroy() throws DestroyFailedException {
        key.destroy();
    }
}
