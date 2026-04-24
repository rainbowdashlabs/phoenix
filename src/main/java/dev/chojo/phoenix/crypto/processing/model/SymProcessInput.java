/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing.model;

import org.jspecify.annotations.Nullable;

/// Represents the input for an AES cryptographic process, including an optional IV.
///
/// @param bytes the bytes to process
/// @param iv    the optional initialization vector
public record SymProcessInput(byte[] bytes, byte @Nullable [] iv) implements ProcessInput {}
