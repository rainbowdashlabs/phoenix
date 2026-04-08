/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.poll;

import net.dv8tion.jda.api.entities.messages.MessagePoll;
import org.jspecify.annotations.Nullable;

public record PollAnswer(@Nullable String emoji, String question, int votes) {
    public static PollAnswer create(MessagePoll.Answer answer) {
        String emoji = null;
        if (answer.getEmoji() != null) {
            emoji = answer.getEmoji().getFormatted();
        }
        return new PollAnswer(emoji, answer.getText(), answer.getVotes());
    }
}
