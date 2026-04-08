import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.chojo.elpis {
    requires net.dv8tion.jda;
    requires okhttp3;
    requires io.github.kaktushose.jdac.core;
    requires io.github.kaktushose.jdac.guice;
    requires dev.goldmensch.fluava;
    requires io.github.kaktushose.proteus;
    requires com.google.guice;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires de.chojo.sadu.postgresql;
    requires de.chojo.sadu.core;
    requires de.chojo.sadu.datasource;
    requires de.chojo.sadu.queries;
    requires de.chojo.sadu.mapper;
    requires de.chojo.sadu.updater;
    requires org.jspecify;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.yaml;
    requires java.desktop;
    requires java.sql;
    requires dev.chojo.ocular;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.collections4;
    requires jdk.jshell;

    exports dev.chojo.configuration;
    exports dev.chojo.core;
    exports dev.chojo.data;
    exports dev.chojo.scan;
    exports dev.chojo.crypto;
    exports dev.chojo.crypto.policy;
    exports dev.chojo.crypto.serialization;
    exports dev.chojo.crypto.processing;
    exports dev.chojo.crypto.processing.wrapper;
    exports dev.chojo.crypto.exceptions;
    exports dev.chojo.crypto.processing.model;
    exports dev.chojo.commands.message;

    opens dev.chojo.core;
    opens dev.chojo.configuration;
    opens dev.chojo.configuration.elements;
    opens dev.chojo.configuration.elements.sub;
}
