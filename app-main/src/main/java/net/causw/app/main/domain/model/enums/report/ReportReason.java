package net.causw.app.main.domain.model.enums.report;

public enum ReportReason {
    SPAM_AD("낚시/놀람/도배"),
    ABUSE_LANGUAGE("욕설/비하"),
    COMMERCIAL_AD("상업적 광고 및 판매"),
    INAPPROPRIATE_CONTENT("음란물/불건전한 만남 및 대화"),
    FRAUD_IMPERSONATION("유출/사칭/사기"),
    OFF_TOPIC("게시판 성격에 부적절함"),
    POLITICAL_CONTENT("정당/정치인 비하 및 선거운동"),
    ILLEGAL_VIDEO("불법촬영물 등의 유통");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}