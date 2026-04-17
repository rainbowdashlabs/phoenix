/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import dev.chojo.crypto.serialization.EncryptedSymAlgorithmWrapper;
import org.jspecify.annotations.Nullable;

/// A record representing encrypted content along with its key and optional IV.
///
/// @param content the base64 encoded encrypted content
/// @param key     the encrypted AES algorithm wrapper
/// @param iv      the optional initialization vector
public record EncryptedContent(
        String content,
        EncryptedSymAlgorithmWrapper key,
        @Nullable String iv) {}
