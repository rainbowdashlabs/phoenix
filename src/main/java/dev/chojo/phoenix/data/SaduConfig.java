/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.phoenix.data;

import com.google.inject.Inject;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import dev.chojo.phoenix.configuration.Configuration;
import dev.chojo.phoenix.configuration.elements.sub.Database;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class SaduConfig {
    private static final Logger log = getLogger(SaduConfig.class);
    private final Configuration configuration;

    @Nullable
    private DataSource dataSource;

    @Inject
    public SaduConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public void init() throws SQLException, IOException {
        connect();
        configure();
        update();
    }

    private void connect() {
        log.info("Connecting to database");
        while (dataSource == null) {
            try {
                internalConnect();
            } catch (Exception e) {
                log.error("Could not connect to database. Trying again in 10 seconds", e);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    private void internalConnect() {
        Database db = configuration.main().database();
        dataSource = DataSourceCreator.create(PostgreSql.get())
                .configure(config -> config.allowMultiQueries(true)
                        .applicationName("Phoenix")
                        .currentSchema(db.schema()))
                .create()
                .withPoolName("Phoenix")
                .usingUsername(db.user())
                .usingPassword(db.password())
                .withMaximumPoolSize(db.poolSize())
                .withMinimumIdle(1)
                .build();
    }

    private void configure() {
        QueryConfiguration config = QueryConfiguration.builder(dataSource)
                .setExceptionHandler(err -> log.error("An error occurred during a database request", err))
                .setThrowExceptions(false)
                .setAtomic(true)
                .setRowMapperRegistry(new RowMapperRegistry().register(PostgresqlMapper.getDefaultMapper()))
                .build();
        QueryConfiguration.setDefault(config);
    }

    private void update() throws SQLException, IOException {
        Database db = configuration.main().database();
        SqlUpdater.builder(dataSource, PostgreSql.get())
                .setReplacements(new QueryReplacement("phoenix_schema.", db.schema() + "."))
                .setSchemas(db.schema())
                .execute();
    }
}
