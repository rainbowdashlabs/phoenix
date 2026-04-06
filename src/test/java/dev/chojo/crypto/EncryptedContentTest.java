/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedContentTest {
    @Test
    void testEncryptedContent() {
        EncryptedAESAlgorithmWrapper key = new EncryptedAESAlgorithmWrapper("key", "cipher");
        EncryptedContent content = new EncryptedContent("content", key, "iv");
        assertEquals("content", content.content());
        assertEquals(key, content.key());
        assertEquals("iv", content.iv());
    }
}
