/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot.message.content.meta.poll;

import dev.chojo.phoenix.data.snapshot.message.content.meta.Meta;
import dev.chojo.phoenix.data.snapshot.message.context.MessageRestorationContext;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a poll's metadata.
 *
 * @param question     The question of the poll.
 * @param answers      The answers of the poll.
 * @param endEpochtime The time when the poll ends in epoch seconds.
 */
public record PollMeta(String question, List<PollAnswer> answers, long endEpochtime) implements Meta {
    private static final int BLOCK_SIZE = 10;
    private static final String BLACK = "⬛";
    private static final String GREEN = "🟩";

    public static PollMeta create(MessagePoll poll) {
        List<PollAnswer> answers =
                poll.getAnswers().stream().map(PollAnswer::create).toList();
        return new PollMeta(
                poll.getQuestion().getText(), answers, poll.getTimeExpiresAt().toEpochSecond());
    }

    @Override
    public void apply(MessageCreateBuilder builder, MessageRestorationContext context) {
        int total = answers().stream().mapToInt(PollAnswer::votes).sum();

        List<TextDisplay> components = new ArrayList<>();
        components.add(TextDisplay.of(question));

        for (PollAnswer answer : answers()) {
            var percent = answer.votes() > 0 ? Math.round(answer.votes() / (float) total * 100) : 0;
            int blocks = percent / 10;
            var blockString = "%s%s %d Votes %d%%"
                    .formatted(GREEN.repeat(blocks), BLACK.repeat(BLOCK_SIZE - blocks), answer.votes(), percent);
            // TODO: Add support for mapping emojis
            var answerText = answer.question() + "\n" + blockString;
            Optional<Emoji> emoji = context.newEmoji(answer.emoji());
            if (emoji.isEmpty()) {
                components.add(TextDisplay.of(answerText));
            } else {
                components.add(TextDisplay.of(emoji.get().getFormatted() + " " + answerText));
            }
            components.add(TextDisplay.of(total + " Votes"));
        }
        builder.setContent(null).useComponentsV2().addComponents(List.of(Container.of(components)));
    }
}
