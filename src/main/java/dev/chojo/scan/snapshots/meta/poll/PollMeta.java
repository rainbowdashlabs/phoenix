package dev.chojo.scan.snapshots.meta.poll;

import dev.chojo.scan.snapshots.meta.Meta;
import net.dv8tion.jda.api.entities.messages.MessagePoll;

import java.util.List;

public record PollMeta(List<PollAnswer> answers) implements Meta {
    public static PollMeta create(MessagePoll poll) {
        List<PollAnswer> answers = poll.getAnswers().stream().map(PollAnswer::create).toList();
        return new PollMeta(answers);
    }

    // TODO: Implement representing this poll as a component
}
