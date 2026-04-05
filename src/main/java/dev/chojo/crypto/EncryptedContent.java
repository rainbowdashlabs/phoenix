package dev.chojo.crypto;

import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;

public record EncryptedContent(String content, EncryptedAESAlgorithmWrapper key) {
}
