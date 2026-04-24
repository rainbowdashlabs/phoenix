/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot;

import dev.chojo.phoenix.crypto.EncryptedContent;

public record EncryptedMessage(
        long guildId, long channelId, long messageId, EncryptedContent content, UserProfile author) {

    public static EncryptedMessage create(EncryptedContent content, MessageSnapshot snapshot) {
        return new EncryptedMessage(
                snapshot.guildId(), snapshot.channelId(), snapshot.messageId(), content, snapshot.author());
    }
}
