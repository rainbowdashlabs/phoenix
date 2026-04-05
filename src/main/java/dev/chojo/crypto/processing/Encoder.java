package dev.chojo.crypto.processing;

import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import javax.crypto.Cipher;

public class Encoder extends Processor {

    public Encoder(AlgorithmWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public byte[] process(byte[] data) {
        return process(data, Cipher.ENCRYPT_MODE);
    }
}
