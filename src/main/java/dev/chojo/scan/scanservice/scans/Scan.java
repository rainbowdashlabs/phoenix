/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice.scans;

import dev.chojo.scan.scanservice.ScanProcess;
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

public interface Scan {
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

    default void scan() {
        next().ifPresent(Scan::scan);
    }

    default Optional<? extends Scan> next() {
        return scans().stream().filter(c -> !c.done()).findFirst();
    }

    default boolean done() {
        return scans().stream().allMatch(Scan::done);
    }

    default int scanned() {
        return scans().stream().mapToInt(Scan::scanned).sum();
    }

    default int maxMessages() {
        return scans().stream().mapToInt(Scan::maxMessages).sum();
    }

    List<? extends Scan> scans();

    dev.chojo.scan.scanservice.ScanProgress progress();
}
