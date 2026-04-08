/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message;

import dev.chojo.data.snapshot.message.content.meta.Meta;

public record Reply(long messageId) implements Meta {}
