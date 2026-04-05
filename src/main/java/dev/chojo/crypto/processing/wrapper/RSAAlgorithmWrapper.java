package dev.chojo.crypto.processing.wrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class RSAAlgorithmWrapper extends AlgorithmWrapper {
    /**
     * The AES key.
     */
    private final Key key;
    private final String cipher;

    public RSAAlgorithmWrapper(Key key) {
        this.key = key;
        this.cipher = "RSA/ECB/PKCS1Padding";
    }
    public RSAAlgorithmWrapper(Key key, String cipher) {
        this.key = key;
        this.cipher = cipher;
    }

    @Override
    public byte[] process(byte[] data, int opMode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return process(data, cipher, opMode, key, null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        RSAAlgorithmWrapper that = (RSAAlgorithmWrapper) o;
        return key.equals(that.key) && cipher.equals(that.cipher);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + cipher.hashCode();
        return result;
    }
}
