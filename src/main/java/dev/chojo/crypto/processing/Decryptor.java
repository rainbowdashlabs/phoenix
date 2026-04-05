/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import javax.crypto.Cipher;

public class Decryptor<I extends ProcessInput, R extends ProcessResult> extends Processor<I, R> {

    public Decryptor(AlgorithmWrapper<I, R> wrapper) {
        if (wrapper.opMode() != Cipher.DECRYPT_MODE) {
            throw new IllegalArgumentException("Wrapper must be in decrypt mode");
        }
        super(wrapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public R process(byte[] data) {
        if (wrapper instanceof AESAlgorithmWrapper) {
            throw new UnsupportedOperationException(
                    "AES decryption requires IV. Use process(AESProcessInput) instead.");
        }
        return process((I) new BytesProcessInput(data));
    }
}
