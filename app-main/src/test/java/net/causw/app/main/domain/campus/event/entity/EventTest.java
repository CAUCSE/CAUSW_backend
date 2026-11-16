package net.causw.app.main.domain.campus.event.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventTest {

	@Test
	@DisplayName("제목, 랜딩 링크, 노출 여부, 순서로 이벤트를 생성한다")
	void givenFields_whenOf_thenCreate() {
		Event event = Event.of("2학기 ICT 위원회 백엔드 신청", "https://causw.net", true, 10);

		assertThat(event.getTitle()).isEqualTo("2학기 ICT 위원회 백엔드 신청");
		assertThat(event.getLandingUrl()).isEqualTo("https://causw.net");
		assertThat(event.getIsActive()).isTrue();
		assertThat(event.getDisplayOrder()).isEqualTo(10);
	}

	@Test
	@DisplayName("이벤트를 수정해도 노출 순서는 바뀌지 않는다")
	void givenEvent_whenUpdate_thenKeepDisplayOrder() {
		Event event = Event.of("제목", "https://causw.net", true, 10);

		event.update("새 제목", "https://new.causw.net", false);

		assertThat(event.getTitle()).isEqualTo("새 제목");
		assertThat(event.getLandingUrl()).isEqualTo("https://new.causw.net");
		assertThat(event.getIsActive()).isFalse();
		assertThat(event.getDisplayOrder()).isEqualTo(10);
	}
}
