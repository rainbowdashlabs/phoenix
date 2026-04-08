/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice.scans;

import dev.chojo.data.snapshot.MessageSnapshot;
import dev.chojo.scan.scanservice.ScanProcess;
import dev.chojo.scan.scanservice.ScanProgress;
import dev.chojo.scan.scanservice.ScanTarget;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
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

        MessageSnapshot.create(message).ifPresent(process::store);
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
