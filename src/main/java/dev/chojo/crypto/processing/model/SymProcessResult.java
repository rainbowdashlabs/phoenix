/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.model;

import org.jspecify.annotations.Nullable;

/// Represents the result of an AES cryptographic process, including an optional IV.
///
/// @param bytes the processed bytes
/// @param iv    the optional initialization vector
public record SymProcessResult(byte[] bytes, byte @Nullable [] iv) implements ProcessResult {}
