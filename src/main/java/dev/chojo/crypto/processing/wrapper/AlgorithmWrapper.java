/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import dev.chojo.crypto.concurrency.LockedCipher;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import org.jspecify.annotations.Nullable;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public abstract class AlgorithmWrapper<I extends ProcessInput, R extends ProcessResult> {
    protected final String cipherName;
    protected final int opMode;

    @Nullable
    private LockedCipher cipher;

    protected AlgorithmWrapper(String cipherName, int opMode) {
        this.cipherName = cipherName;
        this.opMode = opMode;
    }

    public abstract R process(I data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    protected synchronized LockedCipher cipherLock()
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException,
                    InvalidKeyException {
        if (cipher == null) cipher = new LockedCipher(createCipher());
        cipher.lock();
        return cipher;
    }

    protected Cipher createCipher()
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
                    InvalidKeyException {
        return Cipher.getInstance(cipherName);
    }

    public String cipherName() {
        return cipherName;
    }

    public int opMode() {
        return opMode;
    }

    public int processedBytes() {
        return 0;
    }
}
