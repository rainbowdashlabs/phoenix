/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot.message.context;

import dev.chojo.phoenix.data.snapshot.UserProfile;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildRestorationContext {
    private static final Pattern EMOJI_PATTERN = Pattern.compile("<(?<animated>a:)?(?<name>.+):(?<id>[0-9])+>");
    /**
     * Maps channel id to the new channel id.
     */
    private final Function<Long, Long> channelIdMapper;
    /**
     * Maps message id to the new message id.
     */
    private final Function<Long, Long> messageIdMapper;
    /**
     * Maps emoji id or name to the custom emoji on the guild.
     */
    private final BiFunction<String, Long, @Nullable CustomEmoji> emojiMapper;
    /**
     * Maps user id to the user profile.
     */
    private final Function<Long, UserProfile> userMapper;

    public GuildRestorationContext(
            Function<Long, Long> channelIdMapper,
            Function<Long, Long> messageIdMapper,
            BiFunction<String, Long, @Nullable CustomEmoji> emojiMapper,
            Function<Long, UserProfile> userMapper) {
        this.channelIdMapper = channelIdMapper;
        this.messageIdMapper = messageIdMapper;
        this.emojiMapper = emojiMapper;
        this.userMapper = userMapper;
    }

    public Optional<Emoji> newEmoji(String oldId) {
        Matcher matcher = EMOJI_PATTERN.matcher(oldId);
        if (matcher.matches()) {
            return Optional.ofNullable(emojiMapper.apply(matcher.group("name"), Long.parseLong(matcher.group("id"))));
        }
        return Optional.of(Emoji.fromUnicode(oldId));
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
