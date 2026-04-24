/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data.snapshot;

import net.dv8tion.jda.api.entities.User;

public record UserProfile(long id, String username, String profilePicture) {

    public static UserProfile create(User author) {
        return new UserProfile(author.getIdLong(), author.getEffectiveName(), author.getEffectiveAvatarUrl());
    }

    public static UserProfile create(long id) {
        return new UserProfile(
                id, String.valueOf(id), "https://cdn.discordapp.com/embed/avatars/%s.png".formatted(id % 6));
    }
}
