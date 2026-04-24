/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.crypto.processing.model;

/// Represents the result of a byte-based cryptographic process.
///
/// @param bytes the processed bytes
public record BytesProcessResult(byte[] bytes) implements ProcessResult {}
