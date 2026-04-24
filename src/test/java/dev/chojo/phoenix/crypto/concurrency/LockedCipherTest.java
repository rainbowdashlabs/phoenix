/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.concurrency;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static org.junit.jupiter.api.Assertions.*;

class LockedCipherTest {
    @Test
    void testLockUnlock() throws NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        LockedCipher lockedCipher = new LockedCipher(cipher);

        lockedCipher.lock();
        lockedCipher.close(); // unlock

        // Basic test to ensure it doesn't crash
        assertNotNull(lockedCipher.toString());
    }
}
