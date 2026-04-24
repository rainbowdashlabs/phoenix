/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot.message.content.meta;

import dev.chojo.phoenix.data.snapshot.message.context.MessageRestorationContext;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

/**
 * Represents a message that started a thread.
 * The message content itself is not part of this message.
 * This message can be reconstructed by starting a thread from the message id in this message.
 *
 * @param messageId the id of the message that started the thread
 */
public record ThreadStarterMessage(long messageId) implements Meta {

    @Override
    public void apply(MessageCreateBuilder builder, MessageRestorationContext context) {
        // Nothing to do here, the message content is not part of this message
    }
}
