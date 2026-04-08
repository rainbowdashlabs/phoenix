/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice.scans;

import dev.chojo.scan.scanservice.ScanProcess;
import dev.chojo.scan.scanservice.ScanProgress;
import dev.chojo.scan.scanservice.ScanTarget;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.chojo.util.SnowflakeUtil.snowflakeToTimestamp;

/**
 * Represents a scan of a channel that can contain threads, such as a forum or a media channel.
 * <p>
 * This class also handles the threads of a regular text channel if it is used as a sub-scan.
 */
public class ThreadContainerScan implements Scan {
    private ScanProcess scanProcess;
    private final IThreadContainer postContainer;

    @Nullable
    private final ChannelScan parent;

    private final List<ChannelScan> channels;

    private ThreadContainerScan(
            List<ChannelScan> channels, IThreadContainer postContainer, @Nullable ChannelScan parent) {
        this.channels = channels;
        this.postContainer = postContainer;
        this.parent = parent;
    }

    /**
     * Creates a new ThreadContainerScan.
     *
     * @param scanProcess   the overall scan process
     * @param postContainer the container of the threads
     * @param parent        the parent channel scan, if any
     * @param scanned       an optional atomic integer to track the scanned messages
     * @return the created thread container scan
     */
    public static ThreadContainerScan create(
            ScanProcess scanProcess,
            IThreadContainer postContainer,
            @Nullable ChannelScan parent,
            @Nullable AtomicInteger scanned) {
        Set<GuildMessageChannel> threads = new HashSet<>();
        postContainer.retrieveArchivedPublicThreadChannels().stream().forEach(threads::add);
        threads.addAll(postContainer.getThreadChannels());
        List<ChannelScan> list = threads.stream()
                .map(c -> ChannelScan.create(scanProcess, c, scanned))
                // Sort by creation date
                .sorted(Comparator.comparingLong(e -> snowflakeToTimestamp(e.channelId())))
                .toList();
        return new ThreadContainerScan(list, postContainer, parent);
    }

    @Override
    public List<? extends Scan> scans() {
        if (parent != null) {
            // A scan is only valid if the latest parent message was created after the thread was created.
            return channels.stream()
                    .filter(c -> snowflakeToTimestamp(c.channelId()) < parent.currentTimestamp())
                    .toList();
        }
        // If no parent channel is set we assume that the channel is a forum channel that only consists of threads.
        return channels;
    }

    @Override
    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.fromChannelType(postContainer.getType()),
                postContainer.getIdLong(),
                postContainer.getName(),
                scanned(),
                maxMessages(),
                channels.stream().map(Scan::progress).toList());
    }
}
