/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.scan.scanservice.scans;

import dev.chojo.phoenix.scan.scanservice.ScanProcess;
import dev.chojo.phoenix.scan.scanservice.ScanProgress;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a single scan operation.
 * <p>
 * Scans can be nested, e.g., a guild scan contains channel scans, and a category scan contains child channel scans.
 */
public interface Scan {
    /**
     * Creates a {@link Scan} object for the specified channel.
     *
     * @param scanProcess the overall scan process this scan belongs to
     * @param channel     the channel to create a scan for
     * @param scanned     an optional atomic integer to track the total scanned messages across related scans
     * @return an optional containing the created scan, empty if the channel type is not supported
     */
    static Optional<Scan> create(ScanProcess scanProcess, Channel channel, @Nullable AtomicInteger scanned) {
        return switch (channel.getType()) {
            case TEXT, NEWS -> Optional.of(TextChannelScan.create(scanProcess, (StandardGuildMessageChannel) channel));
            case CATEGORY -> Optional.of(CategoryScan.create(scanProcess, (Category) channel));
            case STAGE, VOICE -> Optional.of(ChannelScan.create(scanProcess, (GuildMessageChannel) channel));
            case GUILD_NEWS_THREAD, GUILD_PRIVATE_THREAD, GUILD_PUBLIC_THREAD -> {
                Objects.requireNonNull(scanned, "Scanned must not be null for thread channels");
                yield Optional.of(ChannelScan.create(scanProcess, (ThreadChannel) channel, scanned));
            }
            case FORUM, MEDIA ->
                Optional.of(ThreadContainerScan.create(scanProcess, (IPostContainer) channel, null, null));
            default -> Optional.empty();
        };
    }

    /**
     * Performs the scan operation.
     * <p>
     * The default implementation delegates to the next pending scan.
     */
    default void scan() {
        next().ifPresent(Scan::scan);
    }

    /**
     * Retrieves the next pending child scan.
     *
     * @return the next scan to perform
     */
    default Optional<? extends Scan> next() {
        return scans().stream().filter(c -> !c.done()).findFirst();
    }

    /**
     * Checks if the scan (and all its children) is finished.
     *
     * @return true if the scan is done, false otherwise
     */
    default boolean done() {
        return scans().stream().allMatch(Scan::done);
    }

    /**
     * Gets the number of scanned messages.
     *
     * @return the number of scanned messages
     */
    default int scanned() {
        return scans().stream().mapToInt(Scan::scanned).sum();
    }

    /**
     * Gets the maximum number of messages to scan.
     *
     * @return the maximum number of messages
     */
    default int maxMessages() {
        return scans().stream().mapToInt(Scan::maxMessages).sum();
    }

    /**
     * Gets the list of child scans.
     *
     * @return the child scans
     */
    List<? extends Scan> scans();

    /**
     * Creates a {@link ScanProgress} object for this scan.
     *
     * @return the current progress
     */
    ScanProgress progress();
}
