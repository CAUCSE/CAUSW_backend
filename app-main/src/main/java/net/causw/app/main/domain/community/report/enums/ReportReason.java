package net.causw.app.main.domain.community.report.enums;

public enum ReportReason {
	/** @deprecated 기존 DB 데이터 하위 호환 유지용. 신규 신고에는 사용하지 않음. */
	@Deprecated
	OFF_TOPIC("주제와 무관"),
	SPAM_AD("낚시/놀림/도배"),
	ABUSE_LANGUAGE("욕설/비하"),
	COMMERCIAL_AD("상업적 광고 및 판매"),
	INAPPROPRIATE_CONTENT("음란물/불건전한 만남 및 대화"),
	FRAUD_IMPERSONATION("유출/사칭/사기"),
	POLITICAL_CONTENT("정당/정치인 비하 및 선거운동"),
	ILLEGAL_VIDEO("불법촬영물 등 유통");

	private final String description;

	ReportReason(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}