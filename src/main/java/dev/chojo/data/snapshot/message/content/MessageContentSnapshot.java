/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content;

import dev.chojo.data.snapshot.UserProfile;
import dev.chojo.data.snapshot.message.content.meta.Meta;
import dev.chojo.data.snapshot.message.content.meta.PollEnd;
import dev.chojo.data.snapshot.message.content.meta.Reference;
import dev.chojo.data.snapshot.message.content.meta.ThreadCreated;
import dev.chojo.data.snapshot.message.content.meta.ThreadStarterMessage;
import dev.chojo.data.snapshot.message.content.meta.poll.PollMeta;
import dev.chojo.data.snapshot.message.context.MessageRestorationContext;
import dev.chojo.util.Links;
import dev.chojo.util.SnowflakeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.Nullable;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.entities.MessageType.AUTO_MODERATION_ACTION;
import static net.dv8tion.jda.api.entities.MessageType.CALL;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_FOLLOW_ADD;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_ICON_CHANGE;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_NAME_CHANGE;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_PINNED_ADD;
import static net.dv8tion.jda.api.entities.MessageType.CONTEXT_COMMAND;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_APPLICATION_PREMIUM_SUBSCRIPTION;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_BOOST_TIER_1;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_BOOST_TIER_2;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_BOOST_TIER_3;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_DISCOVERY_DISQUALIFIED;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_DISCOVERY_REQUALIFIED;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_INCIDENT_ALERT_MODE_DISABLED;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_INCIDENT_ALERT_MODE_ENABLED;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_INCIDENT_REPORT_FALSE_ALARM;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_INCIDENT_REPORT_RAID;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_INVITE_REMINDER;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_MEMBER_BOOST;
import static net.dv8tion.jda.api.entities.MessageType.GUILD_MEMBER_JOIN;
import static net.dv8tion.jda.api.entities.MessageType.INTERACTION_PREMIUM_UPSELL;
import static net.dv8tion.jda.api.entities.MessageType.PURCHASE_NOTIFICATION;
import static net.dv8tion.jda.api.entities.MessageType.RECIPIENT_ADD;
import static net.dv8tion.jda.api.entities.MessageType.RECIPIENT_REMOVE;
import static net.dv8tion.jda.api.entities.MessageType.ROLE_SUBSCRIPTION_PURCHASE;
import static net.dv8tion.jda.api.entities.MessageType.SLASH_COMMAND;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_END;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_SPEAKER;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_START;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_TOPIC;

public record MessageContentSnapshot(
        long userId,
        String rawContent,
        @Nullable List<String> components,
        @Nullable List<String> embeds,
        List<Attachment> attachmentURLs,
        boolean pinned,
        @Nullable Meta meta) {
    private static final ComponentSerializer serializer = new ComponentSerializer();
    private static final ComponentDeserializer deserializer = new ComponentDeserializer(Collections.emptyList());
    private static final Set<MessageType> IGNORE_MESSAGE_TYPES = Set.of(
            // Discord system messages
            GUILD_INVITE_REMINDER,
            AUTO_MODERATION_ACTION,
            // Subscription related
            ROLE_SUBSCRIPTION_PURCHASE,
            INTERACTION_PREMIUM_UPSELL,
            // Stage related
            STAGE_START,
            STAGE_END,
            STAGE_SPEAKER,
            STAGE_TOPIC,
            // Subscription purchase
            GUILD_APPLICATION_PREMIUM_SUBSCRIPTION,
            PURCHASE_NOTIFICATION,
            // Guild security
            GUILD_INCIDENT_ALERT_MODE_ENABLED,
            GUILD_INCIDENT_ALERT_MODE_DISABLED,
            GUILD_INCIDENT_REPORT_RAID,
            GUILD_INCIDENT_REPORT_FALSE_ALARM,
            // Private channel stuff
            RECIPIENT_ADD,
            RECIPIENT_REMOVE,
            CALL,
            CHANNEL_NAME_CHANGE,
            CHANNEL_ICON_CHANGE,
            CHANNEL_PINNED_ADD,
            // Guild member related
            GUILD_MEMBER_JOIN,
            GUILD_MEMBER_BOOST,
            GUILD_BOOST_TIER_1,
            GUILD_BOOST_TIER_2,
            GUILD_BOOST_TIER_3,
            // Guild discovery related
            GUILD_DISCOVERY_DISQUALIFIED,
            GUILD_DISCOVERY_REQUALIFIED,
            GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING,
            GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING,
            // We can not backup the target channel from the channel follow add event
            CHANNEL_FOLLOW_ADD,
            // Command-generated messages probably provide little value. We will ignore this.
            SLASH_COMMAND,
            CONTEXT_COMMAND);

    /**
     * Creates a new MessageContentSnapshot from a {@link Message}.
     *
     * @param message the message to create a snapshot for
     * @return an optional containing the created snapshot, empty if the message type is not supported
     */
    public static Optional<MessageContentSnapshot> create(Message message) {
        if (IGNORE_MESSAGE_TYPES.contains(message.getType())) {
            return Optional.empty();
        }

        // Check if a thread was started from this message
        // Serialize the message with extras

        message.getStartedThread();
        long userId = message.getAuthor().getIdLong();
        String rawContent = message.getContentRaw();
        List<String> embeds = message.getEmbeds().stream()
                                     .map(m -> m.toData().toJson())
                                     .map(e -> Base64.getEncoder().encodeToString(e))
                                     .toList();
        List<Attachment> attachmentURLs =
                message.getAttachments().stream().map(Attachment::create).toList();
        List<String> components = null;
        if (message.isUsingComponentsV2()) {
            // TODO Probably find a way to serialize components? Or just dont care?
            // We do not care about v1 components
            components = message.getComponents().stream()
                                .map(serializer::serialize)
                                .map(DataObject::toJson)
                                .map(e -> Base64.getEncoder().encodeToString(e))
                                .toList();
        }
        Meta meta = null;
        switch (message.getType()) {
            // If it is an answer
            case INLINE_REPLY -> {
                meta = new Reference(message.getMessageReference().getMessageIdLong());
            }
            // Poll result
            case POLL_RESULT -> {
                // An ended poll does not contain the finalized votes. However, it references the original poll message.
                meta = new PollEnd(message.getMessageReference().getMessageIdLong());
            }
            //
            case DEFAULT -> {
                // A poll that has been finalized. A poll is a normal message that contains a poll, but no content.
                // We only save finalized polls.
                if (message.getPoll() != null && message.getPoll().isFinalizedVotes()) {
                    message.getPoll().getTimeExpiresAt().toEpochSecond();
                    meta = PollMeta.create(message.getPoll());
                }
                // Even if a thread was created from that message, we will later use it when loading the thread starter
                // message from the original thread.
                // That way we also do not need to recreate an thread start embed. We can just start the thread.
            }
            case THREAD_CREATED -> {
                ThreadChannel startedThread = message.getStartedThread();
                String name = startedThread.getName();
                long idLong = startedThread.getIdLong();
                rawContent = "";
                meta = new ThreadCreated(name, idLong);
            }
            case THREAD_STARTER_MESSAGE -> {
                meta = new ThreadStarterMessage(message.getReferencedMessage().getIdLong());
            }
        }
        boolean pinned = message.isPinned();
        return Optional.of(new MessageContentSnapshot(
                message.getIdLong(), rawContent, components, embeds, attachmentURLs, pinned, meta));
    }

    public MessageCreateData create(MessageRestorationContext context) {
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setContent(rawContent)
                .setAllowedMentions(Collections.emptyList());
        if (components != null) {
            List<DataObject> components = this.components.stream()
                                                         .map(e -> Base64.getDecoder().decode(e))
                                                         .map(DataObject::fromJson)
                                                         .toList();
            List<MessageTopLevelComponent> iComponentUnions = deserializer.deserializeAll(components).stream()
                                                                          .map(MessageTopLevelComponent.class::cast)
                                                                          .toList();
            MessageComponentTree messageComponentTree = MessageComponentTree.of(iComponentUnions);
            MessageComponentTree disabled = messageComponentTree.asDisabled();
            builder.useComponentsV2().addComponents(disabled);
        } else if (embeds != null) {
            List<MessageEmbed> deserialized = embeds.stream()
                                                    .map(e -> Base64.getDecoder().decode(e))
                                                    .map(DataObject::fromJson)
                                                    .map(EmbedBuilder::fromData)
                                                    .map(EmbedBuilder::build)
                                                    .toList();
            builder.addEmbeds(deserialized);
        }

        if (!attachmentURLs.isEmpty()) {
            builder.addContent(
                    "\n" + attachmentURLs.stream().map(Attachment::link).collect(Collectors.joining(" ")));
        }

        // End of message content

        if (meta != null) {
            meta.apply(builder, context);
        }

        UserProfile userProfile = context.userProfile(userId);
        if (!builder.isUsingComponentsV2()) {
            builder.addContent("\n-# <t:%s> by [%s](<%s>)"
                    .formatted(
                            SnowflakeUtil.snowflakeToTimestamp(context.messageId()),
                            userProfile.username(),
                            Links.user(userId)));
        }

        return builder.build();
    }
}
