/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.model;

/// Represents a simple byte-based input for a cryptographic process.
///
/// @param bytes the bytes to process
public record BytesProcessInput(byte[] bytes) implements ProcessInput {}
