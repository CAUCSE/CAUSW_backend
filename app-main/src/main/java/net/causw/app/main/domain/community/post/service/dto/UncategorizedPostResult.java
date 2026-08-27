package net.causw.app.main.domain.community.post.service.dto;

import java.time.LocalDateTime;

/**
 * 성격이 미분류인 크롤링 게시글 목록 항목입니다.
 *
 * @param postId 게시글 식별자
 * @param title 게시글 제목
 * @param boardName 게시판 이름
 * @param createdAt 작성 시각
 */
public record UncategorizedPostResult(
	String postId,
	String title,
	String boardName,
	LocalDateTime createdAt) {
}
