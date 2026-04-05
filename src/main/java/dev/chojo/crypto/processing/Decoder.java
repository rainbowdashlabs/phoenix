package dev.chojo.crypto.processing;

import dev.chojo.crypto.processing.wrapper.AlgorithmWrapper;

import javax.crypto.Cipher;

public class Decoder extends Processor {

    public Decoder(AlgorithmWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public byte[] process(byte[] data) {
        return process(data, Cipher.DECRYPT_MODE);
    }
}
