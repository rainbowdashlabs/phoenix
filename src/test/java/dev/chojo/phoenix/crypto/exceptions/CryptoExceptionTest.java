/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.exceptions;

import dev.chojo.phoenix.crypto.exceptions.CryptoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoExceptionTest {
    @Test
    void testCryptoException() {
        Exception cause = new Exception("cause");
        CryptoException exception = new CryptoException("message", cause);
        assertEquals("message", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
