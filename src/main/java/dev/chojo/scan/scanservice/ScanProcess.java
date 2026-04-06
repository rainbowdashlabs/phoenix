/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice;

import dev.chojo.scan.scanservice.scans.Scan;
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

public class ScanProcess {
    private static final Logger log = getLogger(ScanProcess.class);
    public final Guild guild;
    private final List<Channel> channels;
    private final Function<Channel, Integer> maxChannelMessages;
    private List<? extends Scan> scans;
    private Thread currWorker;
    private Instant start = Instant.now();
    private Instant lastSeen;
    // TODO: probably get from central place
    private ScheduledExecutorService runner;

    public ScanProcess(Guild guild, List<Channel> channels) {
        this.guild = guild;
        this.channels = channels;
        // TODO: Get from server settings
        this.maxChannelMessages = e -> 10000;
    }

    public void init() {
        scans = channels.stream()
                .map(c -> Scan.create(this, c, null))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        runner.execute(this::scanTick);
    }

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

    private void scan() {
        scans.stream().filter(c -> !c.done()).findFirst().ifPresent(Scan::scan);
        currWorker = Thread.currentThread();
        lastSeen = Instant.now();
        save();
    }

    public boolean done() {
        return scans.stream().allMatch(Scan::done);
    }

    public int maxMessages() {
        return scans.stream().mapToInt(Scan::maxMessages).sum();
    }

    public int scanned() {
        return scans.stream().mapToInt(Scan::scanned).sum();
    }

    public List<Channel> channels() {
        return channels;
    }

    public Instant start() {
        return start;
    }

    public ScanProgress progress() {
        return new ScanProgress(
                ScanTarget.GUILD,
                guild.getIdLong(),
                guild.getName(),
                scanned(),
                maxMessages(),
                scans.stream().map(Scan::progress).toList());
    }

    public void save() {
        // TODO
        // guild.scan().saveProgress(progress(), start, done() ? Instant.now() : null);
    }

    public int maxChannelMessages(Channel channel) {
        if (channel instanceof ThreadChannel thread) {
            return maxChannelMessages.apply(thread.getParentMessageChannel());
        }
        return maxChannelMessages.apply(channel);
    }

    public Guild guild() {
        return guild;
    }
}
