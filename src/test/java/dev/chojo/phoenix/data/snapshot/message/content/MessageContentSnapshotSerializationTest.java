/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot.message.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.chojo.phoenix.data.snapshot.message.content.meta.PollEnd;
import dev.chojo.phoenix.data.snapshot.message.content.meta.Reference;
import dev.chojo.phoenix.data.snapshot.message.content.meta.ThreadCreated;
import dev.chojo.phoenix.data.snapshot.message.content.meta.ThreadStarterMessage;
import dev.chojo.phoenix.data.snapshot.message.content.meta.poll.PollAnswer;
import dev.chojo.phoenix.data.snapshot.message.content.meta.poll.PollMeta;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageContentSnapshotSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testReferenceSerialization() throws JsonProcessingException {
        MessageContentSnapshot snapshot = new MessageContentSnapshot(
                123L, "content", null, null, Collections.emptyList(), false, new Reference(456L));

        String json = objectMapper.writeValueAsString(snapshot);
        assertTrue(json.contains("\"type\":\"REFERENCE\""));
        assertTrue(json.contains("\"messageId\":456"));

        MessageContentSnapshot deserialized = objectMapper.readValue(json, MessageContentSnapshot.class);
        assertTrue(deserialized.meta() instanceof Reference);
        assertEquals(456L, ((Reference) deserialized.meta()).messageId());
    }

    @Test
    void testPollEndSerialization() throws JsonProcessingException {
        MessageContentSnapshot snapshot = new MessageContentSnapshot(
                123L, "content", null, null, Collections.emptyList(), false, new PollEnd(789L));

        String json = objectMapper.writeValueAsString(snapshot);
        assertTrue(json.contains("\"type\":\"POLL_END\""));
        assertTrue(json.contains("\"messageId\":789"));

        MessageContentSnapshot deserialized = objectMapper.readValue(json, MessageContentSnapshot.class);
        assertTrue(deserialized.meta() instanceof PollEnd);
        assertEquals(789L, ((PollEnd) deserialized.meta()).messageId());
    }

    @Test
    void testThreadCreatedSerialization() throws JsonProcessingException {
        MessageContentSnapshot snapshot = new MessageContentSnapshot(
                123L, "content", null, null, Collections.emptyList(), false, new ThreadCreated("thread-name", 101L));

        String json = objectMapper.writeValueAsString(snapshot);
        assertTrue(json.contains("\"type\":\"THREAD_CREATED\""));
        assertTrue(json.contains("\"name\":\"thread-name\""));
        assertTrue(json.contains("\"threadId\":101"));

        MessageContentSnapshot deserialized = objectMapper.readValue(json, MessageContentSnapshot.class);
        assertTrue(deserialized.meta() instanceof ThreadCreated);
        assertEquals("thread-name", ((ThreadCreated) deserialized.meta()).name());
        assertEquals(101L, ((ThreadCreated) deserialized.meta()).threadId());
    }

    @Test
    void testThreadStarterMessageSerialization() throws JsonProcessingException {
        MessageContentSnapshot snapshot = new MessageContentSnapshot(
                123L, "content", null, null, Collections.emptyList(), false, new ThreadStarterMessage(202L));

        String json = objectMapper.writeValueAsString(snapshot);
        assertTrue(json.contains("\"type\":\"THREAD_STARTER_MESSAGE\""));
        assertTrue(json.contains("\"messageId\":202"));

        MessageContentSnapshot deserialized = objectMapper.readValue(json, MessageContentSnapshot.class);
        assertTrue(deserialized.meta() instanceof ThreadStarterMessage);
        assertEquals(202L, ((ThreadStarterMessage) deserialized.meta()).messageId());
    }

    @Test
    void testPollMetaSerialization() throws JsonProcessingException {
        PollAnswer answer = new PollAnswer(":thumbsup:", "Yes", 5);
        PollMeta pollMeta = new PollMeta("Question?", List.of(answer), 123456789L);
        MessageContentSnapshot snapshot =
                new MessageContentSnapshot(123L, "content", null, null, Collections.emptyList(), false, pollMeta);

        String json = objectMapper.writeValueAsString(snapshot);
        assertTrue(json.contains("\"type\":\"POLL_META\""));
        assertTrue(json.contains("\"question\":\"Question?\""));
        assertTrue(json.contains("\"endEpochtime\":123456789"));

        MessageContentSnapshot deserialized = objectMapper.readValue(json, MessageContentSnapshot.class);
        assertTrue(deserialized.meta() instanceof PollMeta);
        PollMeta deserializedPoll = (PollMeta) deserialized.meta();
        assertEquals("Question?", deserializedPoll.question());
        assertEquals(1, deserializedPoll.answers().size());
        assertEquals("Yes", deserializedPoll.answers().get(0).question());
        assertEquals(5, deserializedPoll.answers().get(0).votes());
        assertEquals(123456789L, deserializedPoll.endEpochtime());
    }

    @Test
    void testNullMetaSerialization() throws JsonProcessingException {
        MessageContentSnapshot snapshot =
                new MessageContentSnapshot(123L, "content", null, null, Collections.emptyList(), false, null);

        String json = objectMapper.writeValueAsString(snapshot);
        MessageContentSnapshot deserialized = objectMapper.readValue(json, MessageContentSnapshot.class);
        assertEquals(null, deserialized.meta());
    }
}
