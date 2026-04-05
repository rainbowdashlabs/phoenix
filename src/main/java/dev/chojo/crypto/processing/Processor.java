/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.exceptions.CryptoException;
import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Base64;

public abstract class Processor implements Serializable {
    protected final AlgorithmWrapper wrapper;

    public Processor(AlgorithmWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public String processToString(byte[] data) {
        return Base64.getEncoder().encodeToString(process(data));
    }

    public String processToString(String data) {
        return Base64.getEncoder().encodeToString(process(data.getBytes()));
    }

    public byte[] processFromString(String data) {
        return process(Base64.getDecoder().decode(data));
    }

    public String processFromStringToString(String data) {
        return new String(process(Base64.getDecoder().decode(data)));
    }

    public abstract byte[] process(byte[] data);

    protected byte[] process(byte[] data, int opMode) {
        try {
            return wrapper.process(data, opMode);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Could not process", e);
        }
    }

    public AlgorithmWrapper wrapper() {
        return wrapper;
    }
}
