package dev.chojo.crypto.policy;

import dev.chojo.crypto.processing.Encryptor;
import dev.chojo.crypto.processing.model.ProcessInput;
import dev.chojo.crypto.processing.model.ProcessResult;

import java.util.function.Supplier;

public record KeyRotationPolicy(long rotationBytes,
                                Supplier<Encryptor<? extends ProcessInput, ? extends ProcessResult>> rotationSupplier) {
}
