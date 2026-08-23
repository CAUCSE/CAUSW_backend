package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum IntegrationErrorCode implements BaseResponseCode {
	CRAWLING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ITN_500_001", "크롤링 과정중 오류가 발생했습니다."),
	CRAWL_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "ITN_502_001", "크롤링 대상을 가져오지 못했습니다."),
	CRAWL_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ITN_500_002", "크롤링 문서를 파싱하지 못했습니다."),
	CRAWL_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "ITN_500_003", "크롤링 작업이 중단되었습니다."),
	CRAWLER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "ITN_500_004", "등록된 사이트 크롤러가 없습니다."),
	CRAWL_SITE_CONFIG_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "ITN_500_005", "활성화된 크롤링 사이트 설정이 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return code;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return message;
	}

	/** {@inheritDoc} */
	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
