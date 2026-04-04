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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoService {
    private final Configuration configuration;
    private final KeyPairGenerator generator;

    @Inject
    public CryptoService(Configuration configuration) throws NoSuchAlgorithmException {
        this.configuration = configuration;
        generator = KeyPairGenerator.getInstance(key());
        generator.initialize(keySize(), new SecureRandom());
    }

    public KeyPair generateKeyPair() {
        return generator.generateKeyPair();
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
