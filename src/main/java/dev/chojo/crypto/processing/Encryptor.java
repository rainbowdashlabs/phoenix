/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing;

import dev.chojo.crypto.processing.model.SymProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;
import dev.chojo.crypto.processing.wrapper.SymAlgorithmWrapper;
import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import javax.crypto.Cipher;

/// Represents a processor used for encryption.
///
/// @param <I> the input type
/// @param <R> the result type
public class Encryptor<I extends ProcessInput, R extends ProcessResult> extends Processor<I, R> {

    /// Creates a new encryptor with the given [AlgorithmWrapper].
    ///
    /// @param wrapper the algorithm wrapper
    /// @throws IllegalArgumentException if the wrapper is not in encrypt mode
    public Encryptor(AlgorithmWrapper<I, R> wrapper) {
        if (wrapper.opMode() != Cipher.ENCRYPT_MODE) {
            throw new IllegalArgumentException("Wrapper must be in encrypt mode");
        }
        super(wrapper);
    }

    /// Processes the given data.
    ///
    /// @param data the data to process
    /// @return the encryption result
    @Override
    @SuppressWarnings("unchecked")
    public R process(byte[] data) {
        if (wrapper instanceof SymAlgorithmWrapper) {
            return process((I) new SymProcessInput(data, null));
        }
        return process((I) new BytesProcessInput(data));
    }
}
