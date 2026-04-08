/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.service;

import dev.chojo.data.snapshot.message.content.MessageContentSnapshot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;

public class MessageStore extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Optional<MessageContentSnapshot> messageContentSnapshot = MessageContentSnapshot.create(event.getMessage());
    }
}
