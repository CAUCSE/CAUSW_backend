package net.causw.app.main.domain.community.post.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.post.enums.PostCategory;

@DisplayName("PostCategoryClassifier 테스트")
class PostCategoryClassifierTest {
	private final PostCategoryClassifier classifier = new PostCategoryClassifier();

	@Test
	@DisplayName("제목 키워드로 카테고리를 분류한다")
	void classify_shouldClassifyByTitleKeyword() {
		assertThat(classifier.classify("[LG전자] 2026년 하반기 LG전자 신입사원 채용"))
			.isEqualTo(PostCategory.RECRUIT);
		assertThat(classifier.classify("2026 캡스톤 디자인 경진대회 모집 안내"))
			.isEqualTo(PostCategory.EXTERNAL_ACTIVITY);
		assertThat(classifier.classify("2026학년도 의료 인공지능 3차 산업체 전문가 강연 안내"))
			.isEqualTo(PostCategory.EVENT_LECTURE);
		assertThat(classifier.classify("2026-하계 AI·SW융합 학부연구생 프로그램 운영 및 모집 계획"))
			.isEqualTo(PostCategory.RESEARCH);
		assertThat(classifier.classify("2026학년도 2학기 수강신청(학부) 일정 안내"))
			.isEqualTo(PostCategory.ACADEMIC);
	}

	@Test
	@DisplayName("여러 카테고리 키워드가 걸리면 우선순위가 높은 카테고리로 분류한다")
	void classify_shouldResolveMultipleMatchesByPriority() {
		// given: '인턴'(채용)과 '학점'(학사)이 함께 등장
		String title = "[SW교육원] 2026년 ICT 글로벌 학점연계 프로젝트 인턴십 모집";

		// when
		PostCategory result = classifier.classify(title);

		// then: 채용이 학사보다 우선순위가 높다
		assertThat(result).isEqualTo(PostCategory.RECRUIT);
	}

	@Test
	@DisplayName("졸업논문 안내는 연구가 아닌 학사로 분류한다")
	void classify_shouldClassifyThesisNoticeAsAcademic() {
		// given: '논문'이 들어 있으나 학위 과정 안내에 해당한다
		String title = "2026.8월 졸업예정자 졸업논문 대체 TOPCIT성적표 제출 안내";

		// when
		PostCategory result = classifier.classify(title);

		// then
		assertThat(result).isEqualTo(PostCategory.ACADEMIC);
	}

	@Test
	@DisplayName("기관명에 연구가 포함되어도 행사 성격을 우선한다")
	void classify_shouldNotTreatInstituteNameAsResearch() {
		// given: '국가데이터연구원'은 기관명이고 실제 성격은 설명회다
		String title = "대학(원)생 초청 국가데이터연구원 기관 설명회 안내";

		// when
		PostCategory result = classifier.classify(title);

		// then
		assertThat(result).isEqualTo(PostCategory.EVENT_LECTURE);
	}

	@Test
	@DisplayName("5개 분류에 속하지 않는 공지는 미분류로 남긴다")
	void classify_shouldReturnNullWhenNothingMatches() {
		assertThat(classifier.classify("2026년도 서울캠퍼스 예비군 훈련 안내")).isNull();
		assertThat(classifier.classify("서울캠퍼스 셔틀버스 노선 임시 변경 안내")).isNull();
		assertThat(classifier.classify("해킹(피싱)메일 수신 주의 안내")).isNull();
	}

	@Test
	@DisplayName("제목이 없으면 미분류로 남긴다")
	void classify_shouldReturnNullWhenTitleIsMissing() {
		assertThat(classifier.classify(null)).isNull();
		assertThat(classifier.classify("")).isNull();
		assertThat(classifier.classify("   ")).isNull();
	}
}
