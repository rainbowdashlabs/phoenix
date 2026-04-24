/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot.message.content.meta;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.chojo.phoenix.data.snapshot.message.content.meta.poll.PollMeta;
import dev.chojo.phoenix.data.snapshot.message.context.MessageRestorationContext;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PollEnd.class, name = "POLL_END"),
    @JsonSubTypes.Type(value = Reference.class, name = "REFERENCE"),
    @JsonSubTypes.Type(value = ThreadCreated.class, name = "THREAD_CREATED"),
    @JsonSubTypes.Type(value = ThreadStarterMessage.class, name = "THREAD_STARTER_MESSAGE"),
    @JsonSubTypes.Type(value = PollMeta.class, name = "POLL_META")
})
public interface Meta {
    void apply(MessageCreateBuilder builder, MessageRestorationContext context);
}
