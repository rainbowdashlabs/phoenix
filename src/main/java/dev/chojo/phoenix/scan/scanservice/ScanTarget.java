/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.scan.scanservice;

import net.dv8tion.jda.api.entities.channel.ChannelType;

/**
 * Represents the target of a scan.
 */
public enum ScanTarget {
    /**
     * A text channel.
     */
    TEXT,
    /**
     * A guild.
     */
    GUILD,
    /**
     * A forum or media channel.
     */
    FORUM,
    /**
     * A voice or stage channel.
     */
    VOICE,
    /**
     * A thread.
     */
    THREAD,
    /**
     * A category.
     */
    CATEGORY;

    /**
     * Maps a {@link ChannelType} to a {@link ScanTarget}.
     *
     * @param type the channel type to map
     * @return the corresponding scan target
     * @throws IllegalArgumentException if the channel type is not supported
     */
    public static ScanTarget fromChannelType(ChannelType type) {
        return switch (type) {
            case TEXT, NEWS -> TEXT;
            case VOICE, STAGE -> VOICE;
            case CATEGORY -> CATEGORY;
            case GUILD_PRIVATE_THREAD, GUILD_PUBLIC_THREAD, GUILD_NEWS_THREAD -> THREAD;
            case MEDIA, FORUM -> FORUM;
            default -> throw new IllegalArgumentException("Invalid channel target: " + type);
        };
    }
}
