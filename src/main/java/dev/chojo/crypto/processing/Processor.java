/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.exceptions.CryptoException;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import java.security.GeneralSecurityException;
import java.util.Base64;

public abstract class Processor<I extends ProcessInput, R extends ProcessResult> {
    protected final AlgorithmWrapper<I, R> wrapper;

    public Processor(AlgorithmWrapper<I, R> wrapper) {
        this.wrapper = wrapper;
    }

    public String processToString(byte[] data) {
        return Base64.getEncoder().encodeToString(process(data).bytes());
    }

    public String processToString(String data) {
        return Base64.getEncoder().encodeToString(process(data.getBytes()).bytes());
    }

    public byte[] processFromString(String data) {
        return process(Base64.getDecoder().decode(data)).bytes();
    }

    public String processFromStringToString(String data) {
        return new String(process(Base64.getDecoder().decode(data)).bytes());
    }

    public abstract R process(byte[] data);

    public R process(I input) {
        try {
            R result = wrapper.process(input);
            if (result == null) {
                throw new CryptoException("Wrapper returned null", null);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Could not process", e);
        }
    }

    public AlgorithmWrapper<I, R> wrapper() {
        return wrapper;
    }
}
