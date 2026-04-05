package dev.chojo.crypto.processing;

import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;
import dev.chojo.crypto.serialization.EncryptedAESAlgorithmWrapper;

import static dev.chojo.util.Serialization.serializeObject;

public class StringEncoder {
    /**
     * The AES encoder that was used to encrypt the content.
     */
    private final Encoder aes;
    /**
     * The RSA encrypted AES key that was used to encrypt the content.
     */
    private final EncryptedAESAlgorithmWrapper key;

    public StringEncoder(Encoder rsa, Encoder aes) {
        this.aes = aes;
        key = EncryptedAESAlgorithmWrapper.encrypt((AESAlgorithmWrapper) aes.wrapper(), rsa);
    }

    public EncryptedContent encode(String content) {
        String encrypted = aes.processToString(content.getBytes());
        return new EncryptedContent(encrypted, key);
    }
}
