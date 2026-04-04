/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.crypto;

import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.crypto.exceptions.CryptoException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private final Configuration configuration;
    private final KeyPairGenerator rsaGenerator;
    private final KeyGenerator aesGenerator;
    private final KeyFactory rsaFactory;
    private final SecretKeyFactory aesFactory;

    @Inject
    public CryptoService(Configuration configuration) throws NoSuchAlgorithmException {
        this.configuration = configuration;
        rsaGenerator = KeyPairGenerator.getInstance(key());
        rsaGenerator.initialize(keySize(), new SecureRandom());
        aesGenerator = KeyGenerator.getInstance("AES");
        aesGenerator.init(256);
        rsaFactory = KeyFactory.getInstance("RSA");
        aesFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    }

    public KeyPair generateKeyPair() {
        return rsaGenerator.generateKeyPair();
    }

    public byte[] encrypt(String text, PublicKey key) {
        return encrypt(text.getBytes(StandardCharsets.UTF_8), key);
    }

    public byte[] encrypt(byte[] data, PublicKey key) {
        try {
            return process(data, Cipher.ENCRYPT_MODE, key);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Could not encrypt", e);
        }
    }

    public byte[] decrypt(byte[] data, PrivateKey key) {
        try {
            return process(data, Cipher.DECRYPT_MODE, key);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Could not decrypt", e);
        }
    }

    private byte[] process(byte[] data, int opMode, Key key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        Cipher cipher = Cipher.getInstance(cipher());
        cipher.init(opMode, key);
        cipher.update(data);
        return cipher.doFinal();
    }

    public String decryptString(byte[] data, PrivateKey key) {
        return new String(decrypt(data, key), StandardCharsets.UTF_8);
    }

    public String serializeKey(Key key) {
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

    private GCMParameterSpec gcmParameterSpec() {
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }

    private SecretKey randomAESKey() throws InvalidKeySpecException {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        char[] key = Base64.getEncoder().withoutPadding().encodeToString(bytes).toCharArray();
        byte[] salt = secureRandom.generateSeed(16);
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
