package net.causw.domain.model.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 해시 생성을 위한 유틸리티 클래스
 */
public class HashUtil {
    
    /**
     * SHA-256 해시 생성
     * @param input 해시할 문자열
     * @return 16진수 해시 문자열
     */
    public static String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
            // SHA-256은 표준 알고리즘이므로 이 예외는 발생하지 않아야 함
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다", e);
        }
    }
    
    /**
     * 여러 문자열을 결합하여 해시 생성
     * @param inputs 해시할 문자열들
     * @return 16진수 해시 문자열
     */
    public static String generateSHA256(String... inputs) {
        StringBuilder combined = new StringBuilder();
        for (String input : inputs) {
            combined.append(input != null ? input : "");
        }
        return generateSHA256(combined.toString());
    }
} 