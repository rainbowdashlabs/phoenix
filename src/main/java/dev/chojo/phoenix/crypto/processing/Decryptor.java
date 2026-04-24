/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing;

import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.ProcessInput;
import dev.chojo.phoenix.crypto.processing.model.ProcessResult;
import dev.chojo.phoenix.crypto.processing.wrapper.AlgorithmWrapper;
import dev.chojo.phoenix.crypto.processing.wrapper.SymAlgorithmWrapper;

import javax.crypto.Cipher;

/// Represents a processor used for decryption.
///
/// @param <I> the input type
/// @param <R> the result type
public class Decryptor<I extends ProcessInput, R extends ProcessResult> extends Processor<I, R> {

    /// Creates a new decryptor with the given [AlgorithmWrapper].
    ///
    /// @param wrapper the algorithm wrapper
    /// @throws IllegalArgumentException if the wrapper is not in decrypt mode
    public Decryptor(AlgorithmWrapper<I, R> wrapper) {
        if (wrapper.opMode() != Cipher.DECRYPT_MODE) {
            throw new IllegalArgumentException("Wrapper must be in decrypt mode");
        }
        super(wrapper);
    }

    /// Processes the given data.
    ///
    /// @param data the data to process
    /// @return the decryption result
    /// @throws UnsupportedOperationException if AES decryption is attempted without an IV
    @Override
    @SuppressWarnings("unchecked")
    public R process(byte[] data) {
        if (wrapper instanceof SymAlgorithmWrapper) {
            throw new UnsupportedOperationException(
                    "AES decryption requires IV. Use process(AESProcessInput) instead.");
        }
        return process((I) new BytesProcessInput(data));
    }
}
