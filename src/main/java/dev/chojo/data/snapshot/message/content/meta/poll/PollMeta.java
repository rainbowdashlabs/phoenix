/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content.meta.poll;

import dev.chojo.data.snapshot.message.contect.MessageRestorationContext;
import dev.chojo.data.snapshot.message.content.meta.Meta;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;

public record PollMeta(String question, List<PollAnswer> answers) implements Meta {
    private static final int BLOCK_SIZE = 10;
    private static final String BLACK = "⬛";
    private static final String GREEN = "🟩";

    public static PollMeta create(MessagePoll poll) {
        List<PollAnswer> answers =
                poll.getAnswers().stream().map(PollAnswer::create).toList();
        return new PollMeta(poll.getQuestion().getText(), answers);
    }

    @Override
    public void apply(MessageCreateBuilder builder, MessageRestorationContext context) {
        int total = answers().stream().mapToInt(PollAnswer::votes).sum();

        List<TextDisplay> components = new ArrayList<>();
        components.add(TextDisplay.of(question));

        for (PollAnswer answer : answers()) {
            int blocks = Math.round((float) total / answer.votes()) / 10;
            var blockString =
                    "%s%s %d Votes".formatted(GREEN.repeat(blocks), BLACK.repeat(BLOCK_SIZE - blocks), answer.votes());
            components.add(TextDisplay.of(answer.emoji() + " " + answer.question() + "\n" + blockString));
        }

        builder.addComponents(List.of(Container.of(components)));
    }

    // TODO: Implement representing this poll as a component
}
