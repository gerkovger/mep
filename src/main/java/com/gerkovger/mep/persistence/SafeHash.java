package com.gerkovger.mep.persistence;

import com.gerkovger.mep.logging.MepLogger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SafeHash {

    private static final MepLogger log = MepLogger.INSTANCE;

    public static String hashPath(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(path.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            log.info("'{}' -> '{}'", path, hex.toString());
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        return path;
    }
}
