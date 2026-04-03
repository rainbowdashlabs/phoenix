package dev.chojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Bootstrapper {

    private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    void main() {
        try {
            new Elpis(Objects.requireNonNull(System.getenv("BOT_TOKEN"), "Bot token not set!"));
        } catch (InterruptedException e) {
            log.error("Failed to start bot!");
        }
    }
}
