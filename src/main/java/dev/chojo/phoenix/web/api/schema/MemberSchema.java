/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.web.api.schema;

/**
 * Schema representing a Discord member returned by the session user endpoint.
 *
 * @param displayName      the display name of the member
 * @param id        the Discord snowflake ID as a string
 * @param color     the member's color as a hex string (e.g. {@code #ffffff})
 * @param profilePictureUrl the URL of the member's effective avatar
 */
public record MemberSchema(String displayName, String id, String color, String profilePictureUrl) {}
