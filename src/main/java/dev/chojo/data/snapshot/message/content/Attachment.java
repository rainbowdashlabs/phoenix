/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content;

import net.dv8tion.jda.api.entities.Message;

public record Attachment(String url, String filename) {
    public static Attachment create(Message.Attachment attachment) {
        return new Attachment(attachment.getUrl(), attachment.getFileName());
    }

    public String link() {
        return "[%s](%s)".formatted(filename, url);
    }
}
