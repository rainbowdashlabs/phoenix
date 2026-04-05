/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto.processing.model;

import org.jspecify.annotations.Nullable;

public record AESProcessInput(byte[] bytes, byte @Nullable [] iv) implements ProcessInput {
}
