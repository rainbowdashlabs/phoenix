-- Base table where all guilds are stored. This table is updated and managed by triggers.
CREATE TABLE IF NOT EXISTS elpis_schema.guild (
    id          BIGINT                  NOT NULL
        CONSTRAINT guild_pk
            PRIMARY KEY,
    joined      TIMESTAMP DEFAULT now() NOT NULL,
    last_update TIMESTAMP DEFAULT now() NOT NULL,
    guild_left  TIMESTAMP
);

-- General trigger for updating the last_update column.
CREATE FUNCTION elpis_schema.update_last_update(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    new.last_update = now();
    RETURN new;
END;
$$;

-- General trigger for registering guilds.
CREATE FUNCTION elpis_schema.register_guild(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    INSERT
    INTO
        elpis_schema.guild(id)
    VALUES
        (new.guild_id)
    ON CONFLICT(id) DO UPDATE SET last_update = now();
END;
$$;

-- Table for storing the public key and cipher of the guild.
CREATE TABLE IF NOT EXISTS elpis_schema.guild_crypto (
    guild_id    BIGINT                  NOT NULL
        CONSTRAINT guild_crypto_pk
            PRIMARY KEY
        CONSTRAINT guild_crypto_guild_id_fk
            REFERENCES elpis_schema.guild
            ON DELETE CASCADE,
    public_key  TEXT,
    cipher      TEXT,
    last_update TIMESTAMP DEFAULT now() NOT NULL
);

CREATE TRIGGER update_last_update
    BEFORE UPDATE
    ON elpis_schema.guild_crypto
    FOR EACH ROW
EXECUTE PROCEDURE elpis_schema.update_last_update();

CREATE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON elpis_schema.guild_crypto
    FOR STATEMENT
EXECUTE PROCEDURE elpis_schema.register_guild();

-- Trigger for deleting the guild_message_key table when the public key is removed from the guild_crypto table.
-- This essentially means that we can no longer decrypt the messages afterward.
CREATE FUNCTION elpis_schema.drop_depending_keys(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    IF new.public_key IS NULL AND new.cipher IS NULL THEN
        DELETE FROM elpis_schema.guild_message_key WHERE guild_id = old.guild_id;
    END IF;
END;
$$;

CREATE TRIGGER drop_message_keys
    AFTER UPDATE OF public_key, cipher
    ON elpis_schema.guild_crypto
    FOR EACH ROW
EXECUTE PROCEDURE elpis_schema.drop_depending_keys();

-- Table for storing the encrypted messages.
-- Upon deleting the key from the guild_message_key table, the message is also deleted.
CREATE TABLE IF NOT EXISTS elpis_schema.guild_message (
    guild_id       BIGINT  NOT NULL
        CONSTRAINT guild_message_guild_id_fk
            REFERENCES elpis_schema.guild
            ON DELETE CASCADE,
    channel_id     BIGINT  NOT NULL,
    message_id     BIGINT  NOT NULL,
    new_message_id BIGINT,
    user_id        BIGINT  NOT NULL,
    key_id         INTEGER NOT NULL
        CONSTRAINT guild_message_guild_message_key_id_fk
            REFERENCES elpis_schema.guild_message_key (id)
            ON DELETE CASCADE,
    message        TEXT    NOT NULL,
    nonce          TEXT    NOT NULL,
    CONSTRAINT guild_message_pk
        PRIMARY KEY (guild_id, channel_id, message_id)
);

CREATE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON elpis_schema.guild_message
    FOR STATEMENT
EXECUTE PROCEDURE elpis_schema.register_guild();

COMMENT ON COLUMN elpis_schema.guild_message.new_message_id IS 'The new id of the message after it was restored. Might be overwritten at any time again when a restore runs.';

CREATE INDEX IF NOT EXISTS guild_message_guild_id_user_id_index
    ON elpis_schema.guild_message (guild_id, user_id);

CREATE INDEX IF NOT EXISTS guild_message_user_id_index
    ON elpis_schema.guild_message (user_id);

CREATE INDEX guild_message_key_id_index
    ON elpis_schema.guild_message (key_id);

-- Table for storing the encrypted message keys.
CREATE TABLE IF NOT EXISTS elpis_schema.guild_message_key (
    guild_id      BIGINT    NOT NULL
        CONSTRAINT guild_message_key_guild_id_fk
            REFERENCES elpis_schema.guild
            ON DELETE CASCADE,
    id            BIGSERIAL NOT NULL,
    encrypted_key TEXT      NOT NULL,
    cipher        TEXT      NOT NULL,
    CONSTRAINT guild_message_key_pk
        PRIMARY KEY (guild_id, encrypted_key, cipher)
);

CREATE INDEX IF NOT EXISTS guild_message_key_guild_id_index
    ON elpis_schema.guild_message_key (guild_id);

CREATE UNIQUE INDEX IF NOT EXISTS guild_message_key_id_uindex
    ON elpis_schema.guild_message_key (id);

CREATE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON elpis_schema.guild_message_key
    FOR STATEMENT
EXECUTE PROCEDURE elpis_schema.register_guild();

-- Table for storing the users of the guild.
CREATE TABLE IF NOT EXISTS elpis_schema.guild_user (
    guild_id        BIGINT NOT NULL
        CONSTRAINT guild_user_guild_id_fk
            REFERENCES elpis_schema.guild,
    id              BIGINT NOT NULL,
    username        TEXT   NOT NULL,
    profile_picture TEXT   NOT NULL,
    CONSTRAINT guild_user_pk
        PRIMARY KEY (guild_id, id)
);

CREATE INDEX IF NOT EXISTS guild_user_id_index
    ON elpis_schema.guild_user (id);

CREATE TRIGGER register_guild
    AFTER UPDATE OR INSERT
    ON elpis_schema.guild_user
    FOR STATEMENT
EXECUTE PROCEDURE elpis_schema.register_guild();


