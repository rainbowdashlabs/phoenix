/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.scan.scanservice.scans;

import dev.chojo.phoenix.scan.scanservice.ScanProcess;
import dev.chojo.phoenix.scan.scanservice.ScanProgress;
import dev.chojo.phoenix.scan.scanservice.ScanTarget;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

import java.util.List;
import java.util.Optional;

/**
 * Represents a scan of a Discord category, including all its channels.
 */
public class CategoryScan implements Scan {
    private final Category category;
    private final List<Scan> channels;

    /**
     * Creates a new CategoryScan.
     *
     * @param category the category to scan
     * @param channels the scans for the channels in the category
     */
    public CategoryScan(Category category, List<Scan> channels) {
        this.category = category;
        this.channels = channels;
    }

    /**
     * Creates a new CategoryScan for the specified category.
     *
     * @param scanProcess the overall scan process
     * @param category    the category to scan
     * @return the created category scan
     */
    public static CategoryScan create(ScanProcess scanProcess, Category category) {
        List<Optional<Scan>> list = category.getChannels().stream()
                .map(e -> Scan.create(scanProcess, e, null))
                .toList();
        return new CategoryScan(
                category,
                list.stream().filter(Optional::isPresent).map(Optional::get).toList());
    }

    @Override
    public List<Scan> scans() {
        return channels;
    }

    @Override
    public ScanProgress progress() {
        List<ScanProgress> list = channels.stream().map(Scan::progress).toList();
        return new ScanProgress(
                ScanTarget.CATEGORY, category.getIdLong(), category.getName(), scanned(), maxMessages(), list);
    }
}
