/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto;

import dev.chojo.phoenix.crypto.EncryptedContent;
import dev.chojo.phoenix.crypto.serialization.EncryptedSymAlgorithmWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedContentTest {
    @Test
    void testEncryptedContent() {
        EncryptedSymAlgorithmWrapper key = new EncryptedSymAlgorithmWrapper("key", "cipher");
        EncryptedContent content = new EncryptedContent("content", key, "iv");
        assertEquals("content", content.content());
        assertEquals(key, content.key());
        assertEquals("iv", content.iv());
    }
}
