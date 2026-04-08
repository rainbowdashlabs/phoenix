-- Base table where all guilds are stored. This table is updated and managed by triggers.
CREATE TABLE IF NOT EXISTS elpis_schema.guild (
    id          BIGINT                  NOT NULL
        CONSTRAINT guild_pk
            PRIMARY KEY,
    joined      TIMESTAMP DEFAULT now() NOT NULL,
    last_update TIMESTAMP DEFAULT now() NOT NULL,
    guild_left  TIMESTAMP
);




CREATE FUNCTION elpis_schema.update_last_update(
) RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    new.last_update = now();
    RETURN new;
END;
$$;

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
    BEFORE UPDATE ON elpis_schema.guild_crypto
    FOR EACH ROW
    EXECUTE PROCEDURE elpis_schema.update_last_update();

CREATE TRIGGER register_guild
    AFTER UPDATE OR INSERT ON elpis_schema.guild_crypto
    FOR STATEMENT
    EXECUTE PROCEDURE elpis_schema.register_guild();


