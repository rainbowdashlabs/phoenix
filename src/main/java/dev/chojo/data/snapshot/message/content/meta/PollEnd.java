/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content.meta;

import dev.chojo.data.snapshot.message.context.MessageRestorationContext;
import dev.chojo.util.Links;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public record PollEnd(long messageId) implements Meta {

    @Override
    public void apply(MessageCreateBuilder builder, MessageRestorationContext context) {
        String response = "\nThe [poll](<%s>) has ended."
                .formatted(Links.message(context.guildId(), context.newChannelId(), context.newMessageId(messageId)));

        builder.setContent(response);
    }
}
