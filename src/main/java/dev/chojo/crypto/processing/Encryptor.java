/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.processing.model.AESProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import javax.crypto.Cipher;

public class Encryptor<I extends ProcessInput, R extends ProcessResult> extends Processor<I, R> {

    public Encryptor(AlgorithmWrapper<I, R> wrapper) {
        if (wrapper.opMode() != Cipher.ENCRYPT_MODE) {
            throw new IllegalArgumentException("Wrapper must be in encrypt mode");
        }
        super(wrapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public R process(byte[] data) {
        if (wrapper instanceof AESAlgorithmWrapper) {
            return process((I) new AESProcessInput(data, null));
        }
        return process((I) new BytesProcessInput(data));
    }
}
