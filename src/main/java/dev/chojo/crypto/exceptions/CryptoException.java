/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.exceptions;

public class CryptoException extends RuntimeException {
    public CryptoException(String message, Exception cause) {
        super(message, cause);
    }
}
