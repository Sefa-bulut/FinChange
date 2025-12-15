package com.example.finchange.auth.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Test ortamı için RSA key pair oluşturan utility sınıfı
 */
public class TestKeyGenerator {

    private static final String TEST_KEYS_DIR = "src/test/resources/keys";
    private static final String PRIVATE_KEY_PATH = TEST_KEYS_DIR + "/test-private-key.pem";
    private static final String PUBLIC_KEY_PATH = TEST_KEYS_DIR + "/test-public-key.pem";

    /**
     * Test için RSA key pair oluşturur ve dosyalara kaydeder
     */
    public static void generateTestKeys() {
        try {
            createKeysDirectory();

            if (!keyFilesExist()) {
                KeyPair keyPair = AuthKeyPairUtil.generateKeyPair();
                saveKeyPair(keyPair);
                System.out.println("Test JWT key pair başarıyla oluşturuldu: " + TEST_KEYS_DIR);
            } else {
                System.out.println("Test JWT key pair zaten mevcut: " + TEST_KEYS_DIR);
            }
        } catch (Exception e) {
            throw new RuntimeException("Test JWT key pair oluşturulamadı", e);
        }
    }

    private static void createKeysDirectory() throws IOException {
        Path keysPath = Paths.get(TEST_KEYS_DIR);
        if (!Files.exists(keysPath)) {
            Files.createDirectories(keysPath);
        }
    }

    private static boolean keyFilesExist() {
        File privateKeyFile = new File(PRIVATE_KEY_PATH);
        File publicKeyFile = new File(PUBLIC_KEY_PATH);
        return privateKeyFile.exists() && publicKeyFile.exists();
    }

    private static void saveKeyPair(KeyPair keyPair) throws IOException {
        savePrivateKey(keyPair.getPrivate());
        savePublicKey(keyPair.getPublic());
    }

    private static void savePrivateKey(PrivateKey privateKey) throws IOException {
        String privateKeyPem = convertPrivateKeyToPem(privateKey);
        try (FileWriter writer = new FileWriter(PRIVATE_KEY_PATH)) {
            writer.write(privateKeyPem);
        }
    }

    private static void savePublicKey(PublicKey publicKey) throws IOException {
        String publicKeyPem = convertPublicKeyToPem(publicKey);
        try (FileWriter writer = new FileWriter(PUBLIC_KEY_PATH)) {
            writer.write(publicKeyPem);
        }
    }

    private static String convertPrivateKeyToPem(PrivateKey privateKey) {
        return "-----BEGIN PRIVATE KEY-----\n" +
                java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----";
    }

    private static String convertPublicKeyToPem(PublicKey publicKey) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
    }

    /**
     * Test key pair dosyalarını temizler
     */
    public static void cleanupTestKeys() {
        try {
            Files.deleteIfExists(Paths.get(PRIVATE_KEY_PATH));
            Files.deleteIfExists(Paths.get(PUBLIC_KEY_PATH));
            System.out.println("Test JWT key pair dosyaları temizlendi");
        } catch (IOException e) {
            System.err.println("Test key pair dosyaları temizlenirken hata: " + e.getMessage());
        }
    }
}
