/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.concurrency;

import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;

/// A wrapper around [Cipher] that provides thread-safe access using a [ReentrantLock].
/// Implements [AutoCloseable] to facilitate the use of try-with-resources for unlocking.
public class LockedCipher implements AutoCloseable {
    private final Cipher cipher;
    private final ReentrantLock lock = new ReentrantLock();

    /// Creates a new locked cipher.
    ///
    /// @param cipher the cipher to wrap
    public LockedCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    /// Acquires the lock.
    public void lock() {
        lock.lock();
    }

    /// Releases the lock.
    @Override
    public void close() {
        lock.unlock();
    }

    /// Returns the wrapped cipher.
    ///
    /// @return the cipher
    public Cipher cipher() {
        return cipher;
    }
}
