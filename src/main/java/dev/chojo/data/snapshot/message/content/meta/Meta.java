/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.data.snapshot.message.content.meta;

import dev.chojo.data.snapshot.message.contect.MessageRestorationContext;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public interface Meta {
    void apply(MessageCreateBuilder builder, MessageRestorationContext context);
}
