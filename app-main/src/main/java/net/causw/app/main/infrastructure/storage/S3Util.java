package net.causw.app.main.infrastructure.storage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class S3Util {
	private static final String FILE_EXTENSION_SEPARATOR = ".";

	//디폴트 파일 이름 생성자 -> category_원본파일명_생성시간.확장자 : 디비에 url 저장 + 원본파일명 따로 저장
	public static String buildFileName(String originalFileName) {
		int fileExtensionIndex = originalFileName.lastIndexOf(FILE_EXTENSION_SEPARATOR);
		String fileExtension = originalFileName.substring(fileExtensionIndex);
		String fileName = originalFileName.substring(0, fileExtensionIndex);
		String now = String.valueOf(System.currentTimeMillis()); //날짜 생성방식 YYMMDDHHMM 으로 수정하기

		return fileName + "_" + now + fileExtension;
	}

	public static String generateHash(String input) {
		String hash = null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			SecureRandom rand = new SecureRandom();
			byte[] saltBytes = new byte[16];
			rand.nextBytes(saltBytes);
			String salt = new String(saltBytes);
			String msg = input + salt;

			digest.update(msg.getBytes(StandardCharsets.UTF_8));
			byte[] hashByte = digest.digest();
			StringBuilder builder = new StringBuilder();
			for (byte b : hashByte) {
				String hexString = String.format("%02x", b);
				builder.append(hexString);
			}
			hash = builder.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return hash;
	}
}
