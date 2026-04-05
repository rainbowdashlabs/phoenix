/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.crypto.processing.wrapper.AESAlgorithmWrapper;

import java.security.AsymmetricKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private final Configuration configuration;
    private final KeyPairGenerator rsaGenerator;
    private final KeyFactory rsaFactory;
    private final SecretKeyFactory aesFactory;

    @Inject
    public CryptoService(Configuration configuration) throws NoSuchAlgorithmException {
        this.configuration = configuration;
        rsaGenerator = KeyPairGenerator.getInstance(key());
        rsaGenerator.initialize(512, new SecureRandom());
        rsaFactory = KeyFactory.getInstance("RSA");
        aesFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    }

    public KeyPair generateRSAKeyPair() {
        return rsaGenerator.generateKeyPair();
    }

    public String serializeKey(AsymmetricKey key) {
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
        if (key instanceof PublicKey) {
            encoded = "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
        } else if (key instanceof PrivateKey) {
            encoded = "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----";
        }
        return encoded;
    }

    public PrivateKey deserializePrivateKey(String key) throws InvalidKeySpecException {
        key = key.replace("\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .trim();
        byte[] decoded = Base64.getDecoder().decode(key);
        return rsaFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    public PublicKey deserializePublicKey(String key) throws InvalidKeySpecException {
        key = key.replace("\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .trim();
        byte[] decoded = Base64.getDecoder().decode(key);
        return rsaFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    private byte[] iv() {
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public AESAlgorithmWrapper randomAESKey() throws InvalidKeySpecException {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        char[] key = Base64.getEncoder().withoutPadding().encodeToString(bytes).toCharArray();
        byte[] salt = secureRandom.generateSeed(16);
        SecretKey secretKey = generateAESKey(key, salt);
        return new AESAlgorithmWrapper(secretKey, iv());
    }

    public SecretKey generateAESKey(char[] key, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(key, salt, 65536, 256);
        return new SecretKeySpec(aesFactory.generateSecret(pbeKeySpec).getEncoded(), "AES");
    }

    private String cipher() {
        return configuration.main().general().cipher();
    }

    private String key() {
        return configuration.main().general().key();
    }

    private int keySize() {
        return configuration.main().general().keySize();
    }
}
