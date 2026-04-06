/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan;

import dev.chojo.scan.scanservice.ScanProcess;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for managing guild scan processes.
 * <p>
 * This service is responsible for starting and monitoring {@link ScanProcess ScanProcesses} for different guilds.
 * It uses a background watcher to periodically check the status of active scans and save them when finished.
 */
public class ScanService {
    private static final Logger log = getLogger(ScanService.class);
    private final Map<Long, ScanProcess> scanProcesses = new HashMap<>();
    private final ScheduledExecutorService watcher = Executors.newSingleThreadScheduledExecutor();

    /**
     * Creates a new ScanService and starts the background watcher.
     */
    public ScanService() {
        watcher.schedule(this::tick, 2, TimeUnit.SECONDS);
    }

    /**
     * Starts a scan for the given guild if no scan is currently active for it.
     *
     * @param guild the guild to scan
     */
    public void scan(Guild guild) {
        if (scanProcesses.containsKey(guild.getIdLong())) return;
        // TODO: Retrieve backup channels.
        List<Channel> channels = new ArrayList<>();
        channels.addAll(Collections.emptyList()); // Add categories
        ScanProcess scanProcess = new ScanProcess(guild, channels);
        scanProcess.init();
        scanProcesses.put(guild.getIdLong(), scanProcess);
        log.info("Started scan for {}", guild);
    }

    /**
     * Periodically checks all active scan processes.
     * <p>
     * If a process is finished, it is removed from the active scans and its results are saved.
     */
    private void tick() {
        for (ScanProcess process : List.copyOf(scanProcesses.values())) {
            if (process.done()) {
                ScanProcess remove = scanProcesses.remove(process.guild().getIdLong());
                log.info("Finished scan on {}", remove.guild());
                remove.save();
            }
        }
    }

    /**
     * Retrieves the active scan process for a guild.
     *
     * @param guild the guild to get the scan process for
     * @return an optional containing the scan process if active, empty otherwise
     */
    public Optional<ScanProcess> getScanProcess(Guild guild) {
        return Optional.ofNullable(scanProcesses.get(guild.getIdLong()));
    }
}
