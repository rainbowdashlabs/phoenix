/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.util;

public class Links {
    public static String message(long guildId, long channelId, long messageId) {
        return "https://discord.com/channels/%s/%s/%s".formatted(guildId, channelId, messageId);
    }

    public static String channel(long guildId, long channelId) {
        return "https://discord.com/channels/%s/%s".formatted(guildId, channelId);
    }

    public static String user(long userId) {
        return "https://discord.com/users/%s".formatted(userId);
    }
}
