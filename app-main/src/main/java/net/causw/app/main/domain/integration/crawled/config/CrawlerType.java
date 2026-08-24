package net.causw.app.main.domain.integration.crawled.config;

/**
 * 사이트별 공지 파싱 전략을 식별합니다.
 */
public enum CrawlerType {
	/** 중앙대학교 소프트웨어학부 공지입니다. */
	CAU_SW_NOTICE,
	/** 중앙대학교 AI학과 공지입니다. */
	CAU_AI_NOTICE,
	/** 중앙대학교 SW교육원 공지입니다. */
	CAU_SW_EDU_NOTICE,
	/** 중앙대학교 본교 공지 API 기반 게시판입니다. */
	CAU_NOTICE
}
