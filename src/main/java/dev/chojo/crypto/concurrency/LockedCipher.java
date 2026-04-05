/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.concurrency;

import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;

public class LockedCipher implements AutoCloseable {
    private final Cipher cipher;
    private final ReentrantLock lock = new ReentrantLock();

    public LockedCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public void lock() {
        lock.lock();
    }

    @Override
    public void close() {
        lock.unlock();
    }

    public Cipher cipher() {
        return cipher;
    }
}
