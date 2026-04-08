/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.commands.message;

import dev.chojo.data.snapshot.MessageSnapshot;
import dev.chojo.data.snapshot.UserProfile;
import dev.chojo.data.snapshot.message.contect.GuildRestorationContext;
import dev.chojo.data.snapshot.message.contect.MessageRestorationContext;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;

@Interaction
public class Replicate {

    @Command(value = "replicate", type = Type.MESSAGE)
    public void replicate(CommandEvent event, Message target) {
        Optional<MessageSnapshot> messageSnapshot = MessageSnapshot.create(target);
        if (messageSnapshot.isEmpty()) return;
        GuildRestorationContext guildRestorationContext =
                new GuildRestorationContext(i -> i, i -> i, i -> UserProfile.create(target.getAuthor()));
        MessageRestorationContext messageRestorationContext = new MessageRestorationContext(
                guildRestorationContext,
                target.getIdLong(),
                target.getChannel().getIdLong(),
                target.getGuild().getIdLong());
        MessageCreateData replicate = messageSnapshot.get().replicate(messageRestorationContext);
        event.reply(replicate);
    }
}
