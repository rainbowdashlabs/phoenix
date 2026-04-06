/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice.scans;

import dev.chojo.scan.scanservice.ScanProcess;
import dev.chojo.scan.scanservice.ScanProgress;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.chojo.scan.scanservice.ScanTarget.fromChannelType;

public class TextChannelScan implements Scan {
    private final StandardGuildMessageChannel channel;
    private final ThreadContainerScan threadContainerScan;
    private final ChannelScan channelScan;

    public TextChannelScan(
            StandardGuildMessageChannel channel, ThreadContainerScan threadContainerScan, ChannelScan channelScan) {
        this.channel = channel;
        this.threadContainerScan = threadContainerScan;
        this.channelScan = channelScan;
    }

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
