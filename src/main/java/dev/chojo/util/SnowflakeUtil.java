/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.util;

import static net.dv8tion.jda.api.utils.TimeUtil.DISCORD_EPOCH;
import static net.dv8tion.jda.api.utils.TimeUtil.TIMESTAMP_OFFSET;

public class SnowflakeUtil {
    /**
     * Snowflake to epoch timestamp in seconds.
     * @param snowflake discord snowflake
     * @return epoch timestamp in seconds
     */
    public static long snowflakeToTimestamp(long snowflake) {
        return ((snowflake >>> TIMESTAMP_OFFSET) + DISCORD_EPOCH) / 1000L;
    }
}
