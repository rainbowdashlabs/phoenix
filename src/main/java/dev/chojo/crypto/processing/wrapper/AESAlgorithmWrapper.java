/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.wrapper;

import org.jspecify.annotations.Nullable;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AESAlgorithmWrapper extends AlgorithmWrapper {
    /**
     * The AES key.
     */
    private final SecretKey key;

    private final byte[] iv;
    private final String cipher;

    @Nullable
    private transient GCMParameterSpec spec;

    public AESAlgorithmWrapper(SecretKey key, byte[] iv, String cipher) {
        this.key = key;
        this.iv = iv;
        this.cipher = cipher;
    }

    public GCMParameterSpec gcmParameterSpec() {
        if (spec != null) return spec;
        spec = new GCMParameterSpec(128, iv);
        return spec;
    }

    @Override
    public byte[] process(byte[] data, int opMode)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return process(data, cipher, opMode, key, gcmParameterSpec());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AESAlgorithmWrapper that = (AESAlgorithmWrapper) o;
        return key.equals(that.key) && Arrays.equals(iv, that.iv) && cipher.equals(that.cipher);
    }

    public SecretKey key() {
        return key;
    }

    public byte[] iv() {
        return iv;
    }

    public String cipher() {
        return cipher;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + Arrays.hashCode(iv);
        result = 31 * result + cipher.hashCode();
        return result;
    }
}
