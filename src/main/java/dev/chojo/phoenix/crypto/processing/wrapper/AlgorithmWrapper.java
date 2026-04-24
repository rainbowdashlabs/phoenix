/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing.wrapper;

import dev.chojo.phoenix.crypto.concurrency.LockedCipher;
import dev.chojo.phoenix.crypto.processing.model.ProcessInput;
import dev.chojo.phoenix.crypto.processing.model.ProcessResult;
import org.jspecify.annotations.Nullable;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;

/// Base class for algorithm wrappers handling encryption and decryption processes.
///
/// @param <I> the input type for the process
/// @param <R> the result type for the process
public abstract class AlgorithmWrapper<I extends ProcessInput, R extends ProcessResult> {
    /// The name of the cipher to be used.
    protected final String cipherName;

    /// The operation mode (e.g., [Cipher#ENCRYPT_MODE] or [Cipher#DECRYPT_MODE]).
    protected final int opMode;

    /// The locked cipher instance, lazily initialized.
    @Nullable
    private LockedCipher cipher;

    /// Creates a new algorithm wrapper.
    ///
    /// @param cipherName the name of the cipher
    /// @param opMode the operation mode
    protected AlgorithmWrapper(String cipherName, int opMode) {
        this.cipherName = cipherName;
        this.opMode = opMode;
    }

    /// Processes the given data.
    ///
    /// @param data the data to process
    /// @return the result of the process
    /// @throws NoSuchPaddingException if the padding is not supported
    /// @throws NoSuchAlgorithmException if the algorithm is not supported
    /// @throws InvalidKeyException if the key is invalid
    /// @throws InvalidAlgorithmParameterException if the parameters are invalid
    /// @throws IllegalBlockSizeException if the block size is illegal
    /// @throws BadPaddingException if the padding is bad
    public abstract R process(I data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    /// Acquires a [LockedCipher] for the current algorithm.
    ///
    /// @return the locked cipher
    /// @throws InvalidAlgorithmParameterException if the parameters are invalid
    /// @throws NoSuchPaddingException if the padding is not supported
    /// @throws NoSuchAlgorithmException if the algorithm is not supported
    /// @throws InvalidKeyException if the key is invalid
    protected synchronized LockedCipher cipherLock()
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException,
                    InvalidKeyException {
        if (cipher == null) cipher = new LockedCipher(createCipher());
        cipher.lock();
        return cipher;
    }

    /// Creates a new [Cipher] instance for the current algorithm.
    ///
    /// @return the new cipher instance
    /// @throws NoSuchPaddingException if the padding is not supported
    /// @throws NoSuchAlgorithmException if the algorithm is not supported
    /// @throws InvalidAlgorithmParameterException if the parameters are invalid
    /// @throws InvalidKeyException if the key is invalid
    protected Cipher createCipher()
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
                    InvalidKeyException {
        return Cipher.getInstance(cipherName);
    }

    /// Gets the name of the cipher.
    ///
    /// @return the cipher name
    public String cipherName() {
        return cipherName;
    }

    /// Gets the operation mode.
    ///
    /// @return the operation mode
    public int opMode() {
        return opMode;
    }

    /// Gets the number of bytes processed.
    ///
    /// @return the number of processed bytes
    public int processedBytes() {
        return 0;
    }

    /// Destroys the algorithm wrapper and clears sensitive data.
    ///
    /// @throws DestroyFailedException if the destruction fails
    public abstract void destroy() throws DestroyFailedException;
}
