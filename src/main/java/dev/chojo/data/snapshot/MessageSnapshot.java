/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot;

import dev.chojo.data.snapshot.message.content.MessageContentSnapshot;
import dev.chojo.data.snapshot.message.context.MessageRestorationContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;

public record MessageSnapshot(
        long guildId, long channelId, long messageId, MessageContentSnapshot content, UserProfile author) {

    public static Optional<MessageSnapshot> create(Message message) {
        return MessageContentSnapshot.create(message)
                .map(contentSnapshot -> new MessageSnapshot(
                        message.getGuild().getIdLong(),
                        message.getChannel().getIdLong(),
                        message.getIdLong(),
                        contentSnapshot,
                        UserProfile.create(message.getAuthor())));
    }

    public MessageCreateData replicate(MessageRestorationContext context) {
        return content.create(context);
    }
}
