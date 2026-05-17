package net.causw.app.main.domain.user.account.service.dto.result;

public record SocialAccountsResult(
	boolean google,
	boolean kakao,
	boolean apple) {
}
