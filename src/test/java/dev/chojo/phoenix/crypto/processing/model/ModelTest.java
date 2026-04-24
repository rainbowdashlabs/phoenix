/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing.model;

import dev.chojo.phoenix.crypto.processing.model.BytesProcessInput;
import dev.chojo.phoenix.crypto.processing.model.BytesProcessResult;
import dev.chojo.phoenix.crypto.processing.model.SymProcessInput;
import dev.chojo.phoenix.crypto.processing.model.SymProcessResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    void testAESProcessResult() {
        byte[] bytes = new byte[16];
        byte[] iv = new byte[12];
        SymProcessResult result = new SymProcessResult(bytes, iv);
        assertArrayEquals(bytes, result.bytes());
        assertArrayEquals(iv, result.iv());
    }

    @Test
    void testBytesProcessInput() {
        byte[] bytes = new byte[16];
        BytesProcessInput input = new BytesProcessInput(bytes);
        assertArrayEquals(bytes, input.bytes());
    }

    @Test
    void testBytesProcessResult() {
        byte[] bytes = new byte[16];
        BytesProcessResult result = new BytesProcessResult(bytes);
        assertArrayEquals(bytes, result.bytes());
    }

    @Test
    void testAESProcessInput() {
        byte[] bytes = new byte[16];
        byte[] iv = new byte[12];
        SymProcessInput input = new SymProcessInput(bytes, iv);
        assertArrayEquals(bytes, input.bytes());
        assertArrayEquals(iv, input.iv());
    }
}
