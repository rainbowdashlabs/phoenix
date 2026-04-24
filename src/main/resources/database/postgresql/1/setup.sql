-- Base table where all guilds are stored. This table is updated and managed by triggers.
CREATE TABLE IF NOT EXISTS phoenix_schema.guild (
    id          BIGINT                  NOT NULL
        CONSTRAINT guild_pk
            PRIMARY KEY,
    joined      TIMESTAMP DEFAULT now() NOT NULL,
    last_update TIMESTAMP DEFAULT now() NOT NULL,
    guild_left  TIMESTAMP
);

-- General trigger for updating the last_update column.
CREATE OR REPLACE FUNCTION phoenix_schema.update_last_update(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    new.last_update = now();
    RETURN new;
END;
$$;

-- General trigger for registering guilds.
CREATE OR REPLACE FUNCTION phoenix_schema.register_guild(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    INSERT
    INTO
        phoenix_schema.guild(id)
    VALUES
        (new.guild_id)
    ON CONFLICT(id) DO UPDATE SET last_update = now();
    RETURN new;
END;
$$;

-- Table for storing the encrypted message keys.
CREATE TABLE IF NOT EXISTS phoenix_schema.guild_message_key (
    guild_id      BIGINT                  NOT NULL
        CONSTRAINT guild_message_key_guild_id_fk
            REFERENCES phoenix_schema.guild
            ON DELETE CASCADE,
    id            BIGSERIAL               NOT NULL,
    encrypted_key TEXT                    NOT NULL,
    cipher        TEXT                    NOT NULL,
    created       TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT guild_message_key_pk
        PRIMARY KEY (guild_id, encrypted_key, cipher)
);

CREATE INDEX IF NOT EXISTS guild_message_key_guild_id_index
    ON phoenix_schema.guild_message_key (guild_id);

CREATE UNIQUE INDEX IF NOT EXISTS guild_message_key_id_uindex
    ON phoenix_schema.guild_message_key (id);

CREATE OR REPLACE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON phoenix_schema.guild_message_key
    FOR EACH ROW
EXECUTE PROCEDURE phoenix_schema.register_guild();


-- Table for storing the public key and cipher of the guild.
CREATE TABLE IF NOT EXISTS phoenix_schema.guild_crypto (
    guild_id    BIGINT                  NOT NULL
        CONSTRAINT guild_crypto_pk
            PRIMARY KEY
        CONSTRAINT guild_crypto_guild_id_fk
            REFERENCES phoenix_schema.guild
            ON DELETE CASCADE,
    public_key  TEXT,
    cipher      TEXT,
    last_update TIMESTAMP DEFAULT now() NOT NULL
);

CREATE OR REPLACE TRIGGER update_last_update
    AFTER UPDATE
    ON phoenix_schema.guild_crypto
    FOR EACH ROW
EXECUTE PROCEDURE phoenix_schema.update_last_update();

CREATE OR REPLACE TRIGGER register_guild
    BEFORE UPDATE OR INSERT
    ON phoenix_schema.guild_crypto
    FOR EACH ROW
EXECUTE PROCEDURE phoenix_schema.register_guild();

-- Trigger for deleting the guild_message_key table when the public key is removed from the guild_crypto table.
-- This essentially means that we can no longer decrypt the messages afterward.
CREATE OR REPLACE FUNCTION phoenix_schema.drop_depending_keys(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    IF new.public_key IS NULL AND new.cipher IS NULL THEN
        DELETE FROM phoenix_schema.guild_message_key WHERE guild_id = old.guild_id;
    END IF;
    RETURN new;
END;
$$;

CREATE OR REPLACE TRIGGER drop_message_keys
    AFTER UPDATE OF public_key, cipher
    ON phoenix_schema.guild_crypto
    FOR EACH ROW
EXECUTE PROCEDURE phoenix_schema.drop_depending_keys();

-- Table for storing the encrypted messages.
-- Upon deleting the key from the guild_message_key table, the message is also deleted.
CREATE TABLE IF NOT EXISTS phoenix_schema.guild_message (
    guild_id       BIGINT  NOT NULL
        CONSTRAINT guild_message_guild_id_fk
            REFERENCES phoenix_schema.guild
            ON DELETE CASCADE,
    channel_id     BIGINT  NOT NULL,
    message_id     BIGINT  NOT NULL,
    new_message_id BIGINT,
    user_id        BIGINT  NOT NULL,
    key_id         INTEGER NOT NULL
        CONSTRAINT guild_message_guild_message_key_id_fk
            REFERENCES phoenix_schema.guild_message_key (id)
            ON DELETE CASCADE,
    message        TEXT    NOT NULL,
    nonce          TEXT    NOT NULL,
    CONSTRAINT guild_message_pk
        PRIMARY KEY (guild_id, channel_id, message_id)
);

CREATE OR REPLACE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON phoenix_schema.guild_message
    FOR EACH ROW
EXECUTE PROCEDURE phoenix_schema.register_guild();

COMMENT ON COLUMN phoenix_schema.guild_message.new_message_id IS 'The new id of the message after it was restored. Might be overwritten at any time again when a restore runs.';

CREATE INDEX IF NOT EXISTS guild_message_guild_id_user_id_index
    ON phoenix_schema.guild_message (guild_id, user_id);

CREATE INDEX IF NOT EXISTS guild_message_user_id_index
    ON phoenix_schema.guild_message (user_id);

CREATE INDEX IF NOT EXISTS guild_message_key_id_index
    ON phoenix_schema.guild_message (key_id);


-- Table for storing the users of the guild.
CREATE TABLE IF NOT EXISTS phoenix_schema.guild_user (
    guild_id        BIGINT NOT NULL
        CONSTRAINT guild_user_guild_id_fk
            REFERENCES phoenix_schema.guild,
    id              BIGINT NOT NULL,
    username        TEXT   NOT NULL,
    profile_picture TEXT   NOT NULL,
    CONSTRAINT guild_user_pk
        PRIMARY KEY (guild_id, id)
);

CREATE INDEX IF NOT EXISTS guild_user_id_index
    ON phoenix_schema.guild_user (id);

CREATE OR REPLACE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON phoenix_schema.guild_user
    FOR EACH ROW
EXECUTE PROCEDURE phoenix_schema.register_guild();

CREATE TABLE IF NOT EXISTS phoenix_schema.user_token (
    user_id       BIGINT,
    access_token  TEXT                    NOT NULL,
    refresh_token TEXT                    NOT NULL,
    expiry        TIMESTAMP               NOT NULL,
    token         TEXT                    NOT NULL,
    last_used     TIMESTAMP DEFAULT now() NOT NULL,
    scopes        TEXT[]                  NOT NULL
);

CREATE INDEX IF NOT EXISTS user_token_last_used_index
    ON phoenix_schema.user_token (last_used);

CREATE UNIQUE INDEX IF NOT EXISTS user_token_token_uindex
    ON phoenix_schema.user_token (token);

CREATE INDEX IF NOT EXISTS user_token_user_id_index
    ON phoenix_schema.user_token (user_id);

CREATE TABLE IF NOT EXISTS phoenix_schema.subscriptions (
    target_id       BIGINT    NOT NULL,
    subscription_id BIGINT    NOT NULL,
    source          TEXT      NOT NULL,
    target          TEXT      NOT NULL,
    purchase_type   TEXT      NOT NULL,
    ends_at         TIMESTAMP NOT NULL,
    persistent      BOOL      NOT NULL,
    CONSTRAINT subscriptions_pk
        PRIMARY KEY (target_id, target, subscription_id)
);

CREATE TABLE IF NOT EXISTS phoenix_schema.user_mails (
    user_id                BIGINT    NOT NULL,
    source                 TEXT      NOT NULL,
    mail_hash              TEXT      NOT NULL
        CONSTRAINT user_mails_pk
            PRIMARY KEY,
    mail_short             TEXT      NOT NULL,
    verified               BOOL      NOT NULL,
    verification_requested TIMESTAMP NOT NULL,
    verification_code      TEXT      NOT NULL
);

CREATE INDEX IF NOT EXISTS user_mails_user_id_index
    ON phoenix_schema.user_mails (user_id);

CREATE INDEX IF NOT EXISTS user_mails_verification_code_index
    ON phoenix_schema.user_mails (verification_code);

CREATE TABLE IF NOT EXISTS phoenix_schema.kofi_purchase (
    id              SERIAL
        PRIMARY KEY,
    mail_hash       TEXT   NOT NULL,
    key             TEXT   NOT NULL,
    subscription_id BIGINT NOT NULL,
    type            TEXT   NOT NULL,
    expires_at      TIMESTAMP,
    transaction_id  TEXT   NOT NULL,
    guild_id        BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS kofi_purchase_id_indexu
    ON phoenix_schema.kofi_purchase (id);

CREATE INDEX IF NOT EXISTS kofi_purchase_guild_id_index
    ON phoenix_schema.kofi_purchase (guild_id);

CREATE INDEX IF NOT EXISTS kofi_purchase_mail_hash_index
    ON phoenix_schema.kofi_purchase (mail_hash);

CREATE TABLE IF NOT EXISTS phoenix_schema.discord_purchase (
    user_id         BIGINT    NOT NULL,
    sku_id          BIGINT    NOT NULL,
    type            TEXT      NOT NULL,
    target          TEXT      NOT NULL,
    subscription_id BIGINT    NOT NULL,
    entitlement_id  BIGINT    NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    persistent      BOOL      NOT NULL,
    guild_id        BIGINT    NOT NULL
);

CREATE INDEX IF NOT EXISTS discord_purchase_expires_at_index
    ON phoenix_schema.discord_purchase (expires_at);

CREATE INDEX IF NOT EXISTS discord_purchase_guild_id_index
    ON phoenix_schema.discord_purchase (guild_id);

CREATE UNIQUE INDEX IF NOT EXISTS discord_purchase_guild_id_subscription_id_uindex
    ON phoenix_schema.discord_purchase (guild_id, subscription_id);

CREATE INDEX IF NOT EXISTS discord_purchase_user_id_index
    ON phoenix_schema.discord_purchase (user_id);

