/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice.scans;

import dev.chojo.scan.scanservice.ScanProcess;
import dev.chojo.scan.scanservice.ScanProgress;
import dev.chojo.scan.scanservice.ScanTarget;
import dev.chojo.scan.snapshots.meta.Meta;
import dev.chojo.scan.snapshots.meta.Reply;
import dev.chojo.scan.snapshots.meta.poll.PollMeta;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.chojo.util.SnowflakeUtil.snowflakeToTimestamp;
import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Represents a scan of a Discord message channel.
 * <p>
 * This class handles the retrieval and processing of messages in a single channel.
 */
public class ChannelScan implements Scan {
    private static final Logger log = getLogger(ChannelScan.class);
    private final ScanProcess process;
    private final GuildMessageChannel channel;
    private final MessageHistory history;
    private final int maxMessages;
    private final AtomicInteger scanned;
    private long currentTimestamp;
    private boolean done;

    private ChannelScan(ScanProcess process, GuildMessageChannel channel, AtomicInteger scanned) {
        this.process = process;
        this.channel = channel;
        history = channel.getHistory();
        this.scanned = scanned;
        maxMessages = process.maxChannelMessages(channel);
    }

    /**
     * Creates a new ChannelScan.
     *
     * @param process the overall scan process
     * @param channel the channel to scan
     * @param scanned an optional atomic integer to track the scanned messages
     * @return the created channel scan
     */
    public static ChannelScan create(
            ScanProcess process, GuildMessageChannel channel, @Nullable AtomicInteger scanned) {
        return new ChannelScan(process, channel, scanned == null ? new AtomicInteger(0) : scanned);
    }

    /**
     * Creates a new ChannelScan.
     *
     * @param process the overall scan process
     * @param channel the channel to scan
     * @return the created channel scan
     */
    public static ChannelScan create(ScanProcess process, GuildMessageChannel channel) {
        return new ChannelScan(process, channel, new AtomicInteger(0));
    }

    @Override
    public void scan() {
        if (!process.guild().getSelfMember().hasPermission(channel, MESSAGE_HISTORY, VIEW_CHANNEL)) {
            done = true;
            return;
        }
        List<Message> messages;
        try {
            messages = history.retrievePast(100).timeout(30, TimeUnit.SECONDS).complete();
        } catch (Exception e) {
            log.error("Error while scanning channel", e);
            done = true;
            return;
        }

        if (messages.isEmpty()) {
            done = true;
            return;
        }

        for (Message message : messages) {
            try {
                check(message);
            } catch (Exception e) {
                log.error("Error processing message", e);
            }
        }
    }

    /**
     * Processes a single message.
     *
     * @param message the message to process
     */
    private void check(Message message) {
        currentTimestamp = snowflakeToTimestamp(message.getIdLong());
        countScan();

        // Check if a thread was started from this message
        // Serialize the message with extras
        message.getStartedThread();
        long userId = message.getAuthor().getIdLong();
        String rawContent = message.getContentRaw();
        MessageComponentTree componentTree = message.getComponentTree();
        // TODO Probably find a way to serialize components? Or just dont care?
        List<String> attachmentURLs = message.getAttachments().stream()
                                             .map(Message.Attachment::getUrl)
                                             .toList();
        Meta meta = null;
        switch (message.getType()) {
            // If it is an answer
            case INLINE_REPLY -> {
                long messageIdLong = message.getMessageReference().getMessageIdLong();
                meta = new Reply(messageIdLong);
            }
            // Poll result
            case POLL_RESULT -> {
                meta = PollMeta.create(message.getPoll());
            }
            //
            case DEFAULT -> {
                // No meta needed
            }
            case RECIPIENT_ADD, RECIPIENT_REMOVE, CALL, CHANNEL_NAME_CHANGE, CHANNEL_ICON_CHANGE,
                 CHANNEL_PINNED_ADD -> {
                // Private group channel stuff
                return;
            }
            case GUILD_MEMBER_JOIN, GUILD_MEMBER_BOOST, GUILD_BOOST_TIER_1, GUILD_BOOST_TIER_2, GUILD_BOOST_TIER_3 -> {

            }
            case CHANNEL_FOLLOW_ADD -> {
                // TODO Create a list of channels that were followed, but probably as part of the channel backup
            }
            case GUILD_DISCOVERY_DISQUALIFIED, GUILD_DISCOVERY_REQUALIFIED,
                 GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING, GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING -> {
                // Not relevant
                return;
            }
            case THREAD_CREATED -> {
                // Save from which message the thread was created.
            }
            case THREAD_STARTER_MESSAGE -> {
            }
            case SLASH_COMMAND, CONTEXT_COMMAND -> {
                User executingUser = message.getInteractionMetadata().getUser();
                // A regular slash command
                if (message.getInteractionMetadata().getType() == InteractionType.COMMAND) {

                }
                // If this is a context command on a message
                if (message.getMessageReference() != null) {

                }
                // If this is a context command on a user
                if (message.getInteractionMetadata().getTargetUser() != null) {

                }

            }
            case GUILD_INVITE_REMINDER, AUTO_MODERATION_ACTION -> {
                // ignore
                return;
            }
            case ROLE_SUBSCRIPTION_PURCHASE, INTERACTION_PREMIUM_UPSELL -> {
                // ignore
                return;
            }
            case STAGE_START, STAGE_END, STAGE_SPEAKER, STAGE_TOPIC -> {
                // no metadata needed
            }
            case GUILD_APPLICATION_PREMIUM_SUBSCRIPTION, PURCHASE_NOTIFICATION -> {
                // ignore
                return;
            }
            case GUILD_INCIDENT_ALERT_MODE_ENABLED, GUILD_INCIDENT_ALERT_MODE_DISABLED, GUILD_INCIDENT_REPORT_RAID,
                 GUILD_INCIDENT_REPORT_FALSE_ALARM -> {
                // ignore
                return;
            }
            case UNKNOWN -> {
                // ignore
                return;
            }
        }
        boolean pinned = message.isPinned();

        // TODO implement saving of messages
    }

    /**
     * Increments the count of scanned messages.
     */
    public void countScan() {
        scanned.incrementAndGet();
    }

    @Override
    public boolean done() {
        return done || scanned() >= maxMessages;
    }

    @Override
    public int scanned() {
        return scanned.get();
    }

    @Override
    public List<Scan> scans() {
        return List.of(this);
    }

    @Override
    public int maxMessages() {
        return done() ? scanned() : maxMessages;
    }

    @Override
    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.fromChannelType(channel.getType()),
                channel.getIdLong(),
                channel.getName(),
                scanned(),
                maxMessages(),
                List.of());
    }

    /**
     * Gets the ID of the channel being scanned.
     *
     * @return the channel ID
     */
    public long channelId() {
        return channel.getIdLong();
    }

    /**
     * Gets the timestamp of the last scanned message.
     *
     * @return the timestamp as a long
     */
    public long currentTimestamp() {
        return currentTimestamp;
    }
}
