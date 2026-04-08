/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.poll;

import dev.chojo.data.snapshot.message.content.meta.Meta;
import net.dv8tion.jda.api.entities.messages.MessagePoll;

import java.util.List;

public record PollMeta(List<PollAnswer> answers) implements Meta {
    public static PollMeta create(MessagePoll poll) {
        List<PollAnswer> answers =
                poll.getAnswers().stream().map(PollAnswer::create).toList();
        return new PollMeta(answers);
    }

    // TODO: Implement representing this poll as a component
}
