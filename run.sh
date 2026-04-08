gradle run -Dbot.config=config.testing.yaml \
                -Dlog4j2.configurationFile=docker/config/log4j2.testing.xml \
                -Dbot.db.host=localhost \
                -Dbot.api.url=http://localhost:5173 \
                --sun-misc-unsafe-memory-access=allow \
                --enable-native-access=ALL-UNNAMED
