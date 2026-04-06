/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.model;

/// Represents the result of a cryptographic process.
public interface ProcessResult {
    /// Returns the processed bytes.
    ///
    /// @return the processed bytes
    byte[] bytes();
}
