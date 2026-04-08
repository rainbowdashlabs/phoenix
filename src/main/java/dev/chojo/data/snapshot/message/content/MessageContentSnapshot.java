/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content;

import dev.chojo.data.snapshot.message.Reply;
import dev.chojo.data.snapshot.message.contect.MessageRestorationContext;
import dev.chojo.data.snapshot.message.content.meta.Meta;
import dev.chojo.data.snapshot.message.content.meta.poll.PollMeta;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.Nullable;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.dv8tion.jda.api.entities.MessageType.AUTO_MODERATION_ACTION;
import static net.dv8tion.jda.api.entities.MessageType.CALL;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_FOLLOW_ADD;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_ICON_CHANGE;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_NAME_CHANGE;
import static net.dv8tion.jda.api.entities.MessageType.CHANNEL_PINNED_ADD;
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
import static net.dv8tion.jda.api.entities.MessageType.STAGE_END;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_SPEAKER;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_START;
import static net.dv8tion.jda.api.entities.MessageType.STAGE_TOPIC;

public record MessageContentSnapshot(
        long userId,
        String rawContent,
        @Nullable List<String> components,
        List<String> attachmentURLs,
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
            CHANNEL_FOLLOW_ADD);

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
        List<Map<String, Object>> embeds =
                message.getEmbeds().stream().map(m -> m.toData().toMap()).toList();
        List<String> attachmentURLs = message.getAttachments().stream()
                .map(Message.Attachment::getUrl)
                .toList();
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
                meta = new Reply(message.getMessageReference().getMessageIdLong());
            }
            // Poll result
            case POLL_RESULT -> {
                meta = PollMeta.create(message.getPoll());
            }
            //
            case DEFAULT -> {
                // No meta needed
            }

            case CHANNEL_FOLLOW_ADD -> {
                // TODO Create a list of channels that were followed, but probably as part of the channel backup
            }
            case THREAD_CREATED -> {
                // Save from which message the thread was created.
            }
            case THREAD_STARTER_MESSAGE -> {}
            case SLASH_COMMAND, CONTEXT_COMMAND -> {
                User executingUser = message.getInteractionMetadata().getUser();
                // A regular slash command
                if (message.getInteractionMetadata().getType() == InteractionType.COMMAND) {}

                // If this is a context command on a message
                if (message.getMessageReference() != null) {}

                // If this is a context command on a user
                if (message.getInteractionMetadata().getTargetUser() != null) {}
            }
        }
        boolean pinned = message.isPinned();
        return Optional.of(
                new MessageContentSnapshot(message.getIdLong(), rawContent, components, attachmentURLs, pinned, meta));
    }

    public MessageCreateData create(MessageRestorationContext context) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.setContent(rawContent);
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
            builder.addComponents(disabled);
        }

        if (meta != null) {
            meta.apply(builder, context);
        }
        return builder.build();
    }
}
