/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.scan.scanservice.scans;

import dev.chojo.phoenix.scan.scanservice.ScanProcess;
import dev.chojo.phoenix.scan.scanservice.ScanProgress;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.chojo.phoenix.scan.scanservice.ScanTarget.fromChannelType;

/**
 * Represents a scan of a text-based channel.
 * <p>
 * This includes the scanning of messages in the channel itself and any associated threads.
 */
public class TextChannelScan implements Scan {
    private final StandardGuildMessageChannel channel;
    private final ThreadContainerScan threadContainerScan;
    private final ChannelScan channelScan;

    /**
     * Creates a new TextChannelScan.
     *
     * @param channel             the channel to scan
     * @param threadContainerScan the scan for threads in this channel
     * @param channelScan         the scan for messages in this channel
     */
    public TextChannelScan(
            StandardGuildMessageChannel channel, ThreadContainerScan threadContainerScan, ChannelScan channelScan) {
        this.channel = channel;
        this.threadContainerScan = threadContainerScan;
        this.channelScan = channelScan;
    }

    /**
     * Creates a new TextChannelScan for the specified channel.
     *
     * @param scanProcess the overall scan process
     * @param channel     the channel to scan
     * @return the created text channel scan
     */
    public static TextChannelScan create(ScanProcess scanProcess, StandardGuildMessageChannel channel) {
        var scanned = new AtomicInteger(0);
        ChannelScan channelScan = ChannelScan.create(scanProcess, channel, scanned);
        return new TextChannelScan(
                channel, ThreadContainerScan.create(scanProcess, channel, channelScan, scanned), channelScan);
    }

    @Override
    public List<Scan> scans() {
        return List.of(threadContainerScan, channelScan);
    }

    @Override
    public ScanProgress progress() {
        return new ScanProgress(
                fromChannelType(channel.getType()),
                channel.getIdLong(),
                channel.getName(),
                scanned(),
                maxMessages(),
                threadContainerScan.progress().childs());
    }
}
