package dev.chojo.crypto.processing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    void testAESProcessResult() {
        byte[] bytes = new byte[16];
        byte[] iv = new byte[12];
        AESProcessResult result = new AESProcessResult(bytes, iv);
        assertArrayEquals(bytes, result.bytes());
        assertArrayEquals(iv, result.iv());
    }

    @Test
    void testBytesProcessInput() {
        byte[] bytes = new byte[16];
        BytesProcessInput input = new BytesProcessInput(bytes);
        assertArrayEquals(bytes, input.bytes());
    }

    @Test
    void testBytesProcessResult() {
        byte[] bytes = new byte[16];
        BytesProcessResult result = new BytesProcessResult(bytes);
        assertArrayEquals(bytes, result.bytes());
    }

    @Test
    void testAESProcessInput() {
        byte[] bytes = new byte[16];
        byte[] iv = new byte[12];
        AESProcessInput input = new AESProcessInput(bytes, iv);
        assertArrayEquals(bytes, input.bytes());
        assertArrayEquals(iv, input.iv());
    }
}
