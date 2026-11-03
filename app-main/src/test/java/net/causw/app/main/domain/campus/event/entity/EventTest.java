package net.causw.app.main.domain.campus.event.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

class EventTest {

	@Test
	@DisplayName("빈 문자열 랜딩 링크로 생성하면 null 로 저장한다")
	void givenEmptyLandingUrl_whenOf_thenNull() {
		Event event = Event.of("제목", "", true, 0, mock(UuidFile.class));

		assertThat(event.getLandingUrl()).isNull();
	}

	@Test
	@DisplayName("빈 문자열 랜딩 링크로 수정하면 기존 링크를 지운다")
	void givenEmptyLandingUrl_whenUpdate_thenNull() {
		Event event = Event.of("제목", "https://causw.net", true, 0, mock(UuidFile.class));

		event.update("제목", "", true, 0);

		assertThat(event.getLandingUrl()).isNull();
	}

	@Test
	@DisplayName("유효한 랜딩 링크는 그대로 저장한다")
	void givenLandingUrl_whenOf_thenKeep() {
		Event event = Event.of("제목", "https://causw.net", true, 0, mock(UuidFile.class));

		assertThat(event.getLandingUrl()).isEqualTo("https://causw.net");
	}

	@Test
	@DisplayName("이미지를 수정하면 새 이미지로 교체한다")
	void givenNewImage_whenUpdateImage_thenReplace() {
		UuidFile newImage = mock(UuidFile.class);
		Event event = Event.of("제목", null, true, 0, mock(UuidFile.class));

		event.updateImage(newImage);

		assertThat(event.getImage()).isEqualTo(newImage);
	}
}
