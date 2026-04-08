/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.util;

public class Functions {
    public static <T> T identity(T t) {
        return t;
    }

    public static <T, V> T biIdentity(T t, V v) {
        return t;
    }
}
