package net.causw.app.main.domain.user.account.util.masking;

public final class EmailMasker {

	private EmailMasker() {
	}

	/**
	 * 이메일 마스킹 규칙:
	 * 1) '@' 앞 로컬파트의 앞 3글자(최대)를 노출한다.
	 * 2) 마스킹 '*'는 최소 3개를 보장한다.
	 * 3) '@' 뒤 도메인파트는 그대로 유지한다.
	 * ex) "abcdef@cau.ac.kr" -> "abc***@cau.ac.kr"
	 * ex) "ab@cau.ac.kr" -> "ab***@cau.ac.kr"
	 */
	public static String mask(String email) {
		if (email == null || !email.contains("@")) {
			return email;
		}
		String[] parts = email.split("@", 2);
		String localPart = parts[0];
		String domainPart = parts[1];

		int visibleCount = Math.min(3, localPart.length());
		String visible = localPart.substring(0, visibleCount);
		String masked = "*".repeat(Math.max(3, localPart.length() - visibleCount));
		return visible + masked + "@" + domainPart;
	}
}
