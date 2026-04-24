/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.commands.message;

import com.google.inject.Inject;
import dev.chojo.phoenix.MessageStoreService;
import dev.chojo.phoenix.data.snapshot.MessageSnapshot;
import dev.chojo.phoenix.data.snapshot.UserProfile;
import dev.chojo.phoenix.data.snapshot.message.context.GuildRestorationContext;
import dev.chojo.phoenix.data.snapshot.message.context.MessageRestorationContext;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;

@Interaction
public class Replicate {

    private final MessageStoreService messageStoreService;

    @Inject
    public Replicate(MessageStoreService messageStoreService) {
        this.messageStoreService = messageStoreService;
    }

    @Command(value = "replicate", type = Type.MESSAGE)
    public void replicate(CommandEvent event, Message target) {
        event.deferReply();
        Optional<MessageSnapshot> messageSnapshot = MessageSnapshot.create(target);
        if (messageSnapshot.isEmpty()) {
            event.reply("This message cannot be replicated");
            return;
        }

        messageStoreService.store(messageSnapshot.get());

        GuildRestorationContext guildRestorationContext = new GuildRestorationContext(
                i -> i,
                i -> i,
                (name, id) -> event.getGuild().getEmojiById(id),
                i -> UserProfile.create(target.getAuthor()));
        MessageRestorationContext messageRestorationContext = new MessageRestorationContext(
                guildRestorationContext,
                target.getIdLong(),
                target.getChannel().getIdLong(),
                target.getGuild().getIdLong());
        MessageCreateData replicate = messageSnapshot.get().replicate(messageRestorationContext);
        Optional<Webhook> restore = target.getChannel().asTextChannel().retrieveWebhooks().complete().stream()
                .filter(i -> i.getName().equals("restore"))
                .findFirst();
        Webhook webhook = restore.orElseGet(() ->
                target.getChannel().asTextChannel().createWebhook("restore").complete());
        event.reply("Message replicated successfully");
        WebhookClient.createClient(target.getJDA(), webhook.getUrl())
                .sendMessage(replicate)
                .setUsername(messageSnapshot.get().author().username())
                .setAvatarUrl(messageSnapshot.get().author().profilePicture())
                .queue();
    }
}
