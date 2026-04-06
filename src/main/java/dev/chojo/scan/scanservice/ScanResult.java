/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice;

import java.time.Instant;

/**
 * Represents the final result of a scan process.
 *
 * @param progress the progress details at the end of the scan
 * @param start    the time when the scan started
 * @param end      the time when the scan finished
 */
public record ScanResult(ScanProgress progress, Instant start, Instant end) {}
