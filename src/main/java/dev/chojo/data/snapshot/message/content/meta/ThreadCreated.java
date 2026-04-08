/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content.meta;

import dev.chojo.data.snapshot.message.context.MessageRestorationContext;
import dev.chojo.util.Links;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public record ThreadCreated(String name, long threadId) implements Meta {
    @Override
    public void apply(MessageCreateBuilder builder, MessageRestorationContext context) {
        MessageEmbed threadCreated = new EmbedBuilder()
                .setTitle("Thread Created", Links.channel(context.guildId(), context.newChannelId(threadId)))
                .setFooter(name)
                .build();
        builder.addEmbeds(threadCreated);
    }
}
