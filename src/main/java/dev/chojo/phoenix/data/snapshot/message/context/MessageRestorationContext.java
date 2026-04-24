/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot.message.context;

import dev.chojo.phoenix.data.snapshot.UserProfile;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Optional;

public class MessageRestorationContext {
    private final GuildRestorationContext context;
    private final long messageId;
    private final long channelId;
    private final long guildId;

    public MessageRestorationContext(GuildRestorationContext context, long messageId, long channelId, long guildId) {
        this.context = context;
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
    }

    public long guildId() {
        return guildId;
    }

    public long newMessageId() {
        return newMessageId(messageId);
    }

    public long messageId() {
        return messageId;
    }

    public long channelId() {
        return channelId;
    }

    public Long newChannelId(Long oldId) {
        return context.newChannelId(oldId);
    }

    public Long newChannelId() {
        return context.newChannelId(channelId);
    }

    public Long newMessageId(Long oldId) {
        return context.newMessageId(oldId);
    }

    public UserProfile userProfile(Long userId) {
        return context.userProfile(userId);
    }

    public Optional<Emoji> newEmoji(String oldId) {
        return context.newEmoji(oldId);
    }
}
