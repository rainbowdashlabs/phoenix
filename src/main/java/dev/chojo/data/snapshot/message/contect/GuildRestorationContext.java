/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.contect;

import dev.chojo.data.snapshot.UserProfile;

import java.util.function.Function;

public class GuildRestorationContext {
    /**
     * Maps channel id to the new channel id.
     */
    private Function<Long, Long> channelIdMapper;
    /**
     * Maps message id to the new message id.
     */
    private Function<Long, Long> messageIdMapper;
    /**
     * Maps user id to the user profile.
     */
    private Function<Long, UserProfile> userMapper;

    public GuildRestorationContext(
            Function<Long, Long> channelIdMapper,
            Function<Long, Long> messageIdMapper,
            Function<Long, UserProfile> userMapper) {
        this.channelIdMapper = channelIdMapper;
        this.messageIdMapper = messageIdMapper;
        this.userMapper = userMapper;
    }

    public Long newChannelId(Long oldId) {
        return channelIdMapper.apply(oldId);
    }

    public Long newMessageId(Long oldId) {
        return messageIdMapper.apply(oldId);
    }

    public UserProfile userProfile(Long oldId) {
        return userMapper.apply(oldId);
    }
}
