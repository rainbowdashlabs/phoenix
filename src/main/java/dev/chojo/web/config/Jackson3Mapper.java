/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.web.config;

import io.javalin.json.JsonMapper;
import io.javalin.json.PipedStreamExecutor;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JsonEncoding;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SequenceWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.stream.Stream;

public class Jackson3Mapper implements JsonMapper {
    private final ObjectMapper mapper;
    private final PipedStreamExecutor executor;

    public Jackson3Mapper(ObjectMapper mapper) {
        this(mapper, false);
    }

    public Jackson3Mapper(ObjectMapper mapper, boolean useVirtualThreads) {
        this.mapper = mapper;
        this.executor = new PipedStreamExecutor(useVirtualThreads);
    }

    @Override
    public <T> @NonNull T fromJsonString(String json, Type targetType) {
        return mapper.readValue(json, mapper.getTypeFactory().constructType(targetType));
    }

    @Override
    public <T> @NonNull T fromJsonStream(InputStream json, Type targetType) {
        return mapper.readValue(json, mapper.getTypeFactory().constructType(targetType));
    }

    @Override
    public String toJsonString(Object obj, Type type) {
        return switch (obj) {
            case String s -> s;
            case Object o -> mapper.writeValueAsString(o);
        };
    }

    @Override
    public InputStream toJsonStream(Object obj, Type type) {
        if (Objects.requireNonNull(obj) instanceof String s) {
            return new ByteArrayInputStream(s.getBytes());
        }
        return executor.getInputStream(out -> mapper.tokenStreamFactory()
                .createGenerator(out, JsonEncoding.UTF8)
                .writePOJO(obj));
    }

    @Override
    public void writeToOutputStream(Stream<?> stream, OutputStream outputStream) {
        SequenceWriter sequenceWriter = mapper.writer().writeValuesAsArray(outputStream);
        stream.forEach(sequenceWriter::write);
    }
}
