package net.causw.app.main.domain.community.post.util;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.post.enums.PostCategory;

/**
 * 소식 게시글의 성격을 제목 키워드로 분류합니다.
 *
 * <p>규칙을 우선순위 순으로 평가해 먼저 걸리는 카테고리로 확정하며, 어느 규칙에도
 * 걸리지 않으면 미분류(null)로 남겨 관리자 수동 수정 대상이 됩니다.</p>
 */
@Component
public class PostCategoryClassifier {

	// 우선순위 순서 (앞선 규칙이 먼저 매칭)
	private static final List<CategoryRule> RULES = List.of(
		new CategoryRule(PostCategory.RECRUIT,
			List.of("채용", "인턴", "취업", "입사", "신입사원", "리크루팅")),
		new CategoryRule(PostCategory.EXTERNAL_ACTIVITY,
			List.of("공모전", "경진대회", "해커톤", "아이디어톤", "서포터즈", "봉사",
				"대외활동", "창업", "동아리", "멘토링", "아카데미", "오디션", "교류캠프")),
		new CategoryRule(PostCategory.EVENT_LECTURE,
			List.of("특강", "세미나", "설명회", "워크숍", "행사", "박람회", "강연",
				"심포지엄", "간담회", "웨비나", "입학식", "학술제", "토크콘서트", "오픈랩", "발표대회")),
		new CategoryRule(PostCategory.RESEARCH,
			List.of("학부연구생", "연구생", "연구조교", "연구실", "연구그룹", "연구과제")),
		new CategoryRule(PostCategory.ACADEMIC,
			List.of("수강", "학점", "졸업", "등록", "휴학", "복학", "장학", "계절학기", "수업",
				"시험", "강의", "성적", "학위", "이수", "교과", "상담", "재입학", "다전공",
				"전공트랙", "폐강", "학사", "논문", "캡스톤디자인", "튜터링", "TOPCIT",
				"공학인증", "개설과목")));

	/**
	 * 제목으로 게시글 성격을 분류합니다.
	 *
	 * @param title 게시글 제목. 없으면 null 가능
	 * @return 분류된 카테고리. 어느 규칙에도 매칭되지 않으면 null(미분류)
	 */
	public PostCategory classify(String title) {
		if (title == null || title.isBlank()) {
			return null;
		}

		String normalizedTitle = title.trim();
		for (CategoryRule rule : RULES) {
			if (rule.keywords().stream().anyMatch(normalizedTitle::contains)) {
				return rule.category();
			}
		}

		return null;
	}

	private record CategoryRule(PostCategory category, List<String> keywords) {
	}
}
