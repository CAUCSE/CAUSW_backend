package net.causw.domain.model.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    
    /**
     * SHA-256 해시 생성
     * @param input 해시할 문자열
     * @return 16진수 해시 문자열
     */
    public static String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(StaticValue.HASH_ALGORITHM);
            byte[] hashBytes = digest.digest((input != null ? input : "").getBytes());
            
            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(MessageUtil.HASH_ALGORITHM_NOT_FOUND, e);
        }
    }
} 