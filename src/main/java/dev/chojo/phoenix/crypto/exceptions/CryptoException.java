/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.exceptions;

import org.jspecify.annotations.Nullable;

/// Exception thrown when a cryptographic operation fails.
public class CryptoException extends RuntimeException {
    /// Creates a new crypto exception with the specified message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause of the exception
    public CryptoException(String message, @Nullable Exception cause) {
        super(message, cause);
    }
}
