/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public abstract class AlgorithmWrapper implements Serializable {
    public abstract byte[] process(byte[] data, int opMode)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    protected byte[] process(byte[] data, String cipher, int opMode, Key key, @Nullable AlgorithmParameterSpec spec)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        Cipher c = Cipher.getInstance(cipher);
        if (spec == null) {
            c.init(opMode, key);
        } else {
            c.init(opMode, key, spec);
        }
        return c.doFinal(data);
    }
}
