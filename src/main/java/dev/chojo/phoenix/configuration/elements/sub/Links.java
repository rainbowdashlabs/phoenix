/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.configuration.elements.sub;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Links {
    private String invite =
            "https://discord.com/oauth2/authorize?client_id={client_id}&scope=bot&permissions=1342532672";
    private String support = "https://discord.gg/phoenix-bot";
    private String kofi = "https://ko-fi.com/phoenix-bot";

    public String invite() {
        return invite;
    }

    public Links resolve(String clientId) {
        Links resolved = new Links();
        resolved.invite = invite.replace("{client_id}", clientId);
        resolved.support = support;
        resolved.kofi = kofi;
        return resolved;
    }

    public String support() {
        return support;
    }

    public String kofi() {
        return kofi;
    }
}
