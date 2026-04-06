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

/// Represents a base class for processing cryptographic data using an [AlgorithmWrapper].
///
/// @param <I> the input type
/// @param <R> the result type
public abstract class Processor<I extends ProcessInput, R extends ProcessResult> {
    /// The algorithm wrapper used for processing.
    protected final AlgorithmWrapper<I, R> wrapper;

    /// Creates a new processor with the given [AlgorithmWrapper].
    ///
    /// @param wrapper the algorithm wrapper
    public Processor(AlgorithmWrapper<I, R> wrapper) {
        this.wrapper = wrapper;
    }

    /// Processes the given data and returns the result as a Base64 encoded string.
    ///
    /// @param data the data to process
    /// @return the Base64 encoded result
    public String processToString(byte[] data) {
        return Base64.getEncoder().encodeToString(process(data).bytes());
    }

    /// Processes the given string data and returns the result as a Base64 encoded string.
    ///
    /// @param data the string data to process
    /// @return the Base64 encoded result
    public String processToString(String data) {
        return Base64.getEncoder().encodeToString(process(data.getBytes()).bytes());
    }

    /// Processes the given Base64 encoded string and returns the result as a byte array.
    ///
    /// @param data the Base64 encoded data to process
    /// @return the processed bytes
    public byte[] processFromString(String data) {
        return process(Base64.getDecoder().decode(data)).bytes();
    }

    /// Processes the given Base64 encoded string and returns the result as a string.
    ///
    /// @param data the Base64 encoded data to process
    /// @return the processed string
    public String processFromStringToString(String data) {
        return new String(process(Base64.getDecoder().decode(data)).bytes());
    }

    /// Processes the given byte array.
    ///
    /// @param data the data to process
    /// @return the process result
    public abstract R process(byte[] data);

    /// Processes the given [ProcessInput].
    ///
    /// @param input the input to process
    /// @return the process result
    /// @throws CryptoException if processing fails
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

    /// Returns the algorithm wrapper.
    ///
    /// @return the algorithm wrapper
    public AlgorithmWrapper<I, R> wrapper() {
        return wrapper;
    }
}
