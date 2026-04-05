/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;

public record EncryptedContent(String content, EncryptedAESAlgorithmWrapper key) {}
