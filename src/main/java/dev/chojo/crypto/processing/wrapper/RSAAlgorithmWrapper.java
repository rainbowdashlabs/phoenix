/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;

public class RSAAlgorithmWrapper extends AlgorithmWrapper<BytesProcessInput, BytesProcessResult> {
    /// The AES key.
    private final Key key;

    private final int opMode;

    public RSAAlgorithmWrapper(Key key, String cipher) {
        this(key, cipher, 0);
    }

    public RSAAlgorithmWrapper(Key key, String cipher, int opMode) {
        super(cipher, opMode);
        this.key = key;
        this.opMode = opMode;
    }

    @Override
    public BytesProcessResult process(BytesProcessInput input)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        try (var cipherLock = cipherLock()) {
            Cipher cipher = cipherLock.cipher();
            cipher.init(opMode, key);
            return new BytesProcessResult(cipher.doFinal(input.bytes()));
        }
    }

    public Key key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RSAAlgorithmWrapper that = (RSAAlgorithmWrapper) o;
        return java.util.Objects.equals(key, that.key) && java.util.Objects.equals(cipherName, that.cipherName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, cipherName);
    }

    @Override
    public void destroy() throws DestroyFailedException {
        // ignore
    }
}
