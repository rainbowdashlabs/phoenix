/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.scan.scanservice;

import java.time.Instant;

public record ScanResult(ScanProgress progress, Instant start, Instant end) {}
