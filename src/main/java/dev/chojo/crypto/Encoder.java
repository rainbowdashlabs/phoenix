package dev.chojo.crypto;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class Encoder {
    /**
     * Key used to encrypt the secret key
     */
    PublicKey publicKey;
    /**
     * Secret key used to encrypt the content.
     */
    SecretKey secretKey;
}
