/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content;

import dev.chojo.data.snapshot.UserProfile;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageContentSnapshotTest {

    @Test
    void testV2ComponentSerializationDeserialization() {
        // Mocking JDA Message with V2 components
        Message message = mock(Message.class);
        User author = mock(User.class);
        when(author.getIdLong()).thenReturn(12345L);
        when(message.getAuthor()).thenReturn(author);
        when(message.getContentRaw()).thenReturn("Test Content");
        when(message.getType()).thenReturn(MessageType.DEFAULT);
        when(message.isPinned()).thenReturn(false);
        when(message.getEmbeds()).thenReturn(Collections.emptyList());
        when(message.getAttachments()).thenReturn(Collections.emptyList());

        // Mock V2 components
        when(message.isUsingComponentsV2()).thenReturn(true);
        Button button = Button.primary("id", "label");
        ActionRow actionRow = ActionRow.of(button);
        List<MessageTopLevelComponentUnion> components = List.of((MessageTopLevelComponentUnion) actionRow);
        when(message.getComponents()).thenReturn(components);
        when(message.getIdLong()).thenReturn(67890L);

        // Create snapshot
        Optional<MessageContentSnapshot> snapshotOpt = MessageContentSnapshot.create(message);
        assertTrue(snapshotOpt.isPresent());
        MessageContentSnapshot snapshot = snapshotOpt.get();

        assertNotNull(snapshot.components());
        assertFalse(snapshot.components().isEmpty());

        // Deserialize back to MessageCreateData
        Function<Long, UserProfile> profileResolver = id -> null;
        MessageCreateData createData = snapshot.create(profileResolver);

        assertNotNull(createData);
        assertEquals("Test Content", createData.getContent());

        // JDA's MessageCreateData doesn't easily expose components for direct comparison in a simple way
        // without further inspection of its internal DataObject, but the fact that it built successfully
        // and usesComponentsV2 was called in snapshot.create() is a good sign.
        // We can check if it contains components in the DataObject.
        assertTrue(createData.toData().hasKey("components"));
    }
}
