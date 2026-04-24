/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.scan.scanservice;

import dev.chojo.phoenix.data.snapshot.MessageSnapshot;
import dev.chojo.phoenix.scan.ScanService;
import dev.chojo.phoenix.scan.scanservice.scans.Scan;
import dev.chojo.phoenix.service.MessageStoreService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Represents an ongoing scan process for a guild.
 * <p>
 * This class coordinates the scanning of all channels within a guild and manages the overall progress.
 */
public class ScanProcess {
    private static final Logger log = getLogger(ScanProcess.class);
    public final Guild guild;
    private final List<Channel> channels;
    private final Function<Channel, Integer> maxChannelMessages;
    private final ScanService scanService;
    private final Function<Channel, Long> oldestKnownMessage;
    private final MessageStoreService messageService;
    private List<? extends Scan> scans;
    private Thread currWorker;
    private Instant start = Instant.now();
    private Instant lastSeen;
    // TODO: probably get from central place
    private ScheduledExecutorService runner;

    /**
     * Creates a new ScanProcess for the specified guild.
     *
     * @param guild    the guild to scan
     * @param channels the list of channels to scan
     */
    public ScanProcess(
            Guild guild, List<Channel> channels, ScanService scanService, MessageStoreService messageService) {
        this.guild = guild;
        this.channels = channels;
        // TODO: Get from server settings
        this.maxChannelMessages = scanService::maxChannelMessages;
        this.scanService = scanService;
        // TODO receive from database
        this.oldestKnownMessage = messageService::oldestKnownMessage;
        this.messageService = messageService;
    }

    /**
     * Initializes the scan process by creating the necessary {@link Scan} objects and starting the runner.
     */
    public void init() {
        scans = channels.stream()
                .map(c -> Scan.create(this, c, null))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        runner.execute(this::scanTick);
    }

    public void store(MessageSnapshot e) {
        messageService.store(e);
    }

    /**
     * Periodically executes a scan tick.
     */
    private void scanTick() {
        var start = Instant.now();
        try {
            scan();
        } catch (Exception e) {
            log.error("Critical error while scanning", e);
        }
        // Wait till next second
        var next = Math.max(0, 1000 - start.until(Instant.now(), ChronoUnit.MILLIS));
        runner.schedule(this::scanTick, next, TimeUnit.MILLISECONDS);
    }

    /**
     * Performs a single scanning operation.
     */
    private void scan() {
        scans.stream().filter(c -> !c.done()).findFirst().ifPresent(Scan::scan);
        currWorker = Thread.currentThread();
        lastSeen = Instant.now();
        save();
    }

    /**
     * Checks if the entire scan process is finished.
     *
     * @return true if all channels are scanned, false otherwise
     */
    public boolean done() {
        return scans.stream().allMatch(Scan::done);
    }

    /**
     * Gets the total number of messages to scan.
     *
     * @return the maximum number of messages
     */
    public int maxMessages() {
        return scans.stream().mapToInt(Scan::maxMessages).sum();
    }

    /**
     * Gets the total number of messages scanned so far.
     *
     * @return the number of scanned messages
     */
    public int scanned() {
        return scans.stream().mapToInt(Scan::scanned).sum();
    }

    /**
     * Gets the list of channels being scanned.
     *
     * @return the list of channels
     */
    public List<Channel> channels() {
        return channels;
    }

    /**
     * Gets the start time of the scan process.
     *
     * @return the start instant
     */
    public Instant start() {
        return start;
    }

    /**
     * Creates a {@link ScanProgress} object representing the current state of the process.
     *
     * @return the current progress
     */
    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.GUILD,
                guild.getIdLong(),
                guild.getName(),
                scanned(),
                maxMessages(),
                scans.stream().map(Scan::progress).toList());
    }

    /**
     * Saves the current progress of the scan.
     */
    public void save() {
        // TODO
        // guild.scan().saveProgress(progress(), start, done() ? Instant.now() : null);
    }

    /**
     * Gets the maximum number of messages to scan for a specific channel.
     *
     * @param channel the channel to check
     * @return the maximum number of messages
     */
    public int maxChannelMessages(Channel channel) {
        if (channel instanceof ThreadChannel thread) {
            return scanService.maxChannelMessages(thread.getParentMessageChannel());
        }
        return scanService.maxChannelMessages(channel);
    }

    /**
     * Receive a timestamp of the last saved message in this channel.
     *
     * @param channel the channel to check
     * @return the timestamp of the last saved message or 0 if no message has been saved yet
     */
    public long earliestKnownMessage(Channel channel) {
        return messageService.oldestKnownMessage(channel);
    }

    /**
     * Gets the guild being scanned.
     *
     * @return the guild
     */
    public Guild guild() {
        return guild;
    }
}
