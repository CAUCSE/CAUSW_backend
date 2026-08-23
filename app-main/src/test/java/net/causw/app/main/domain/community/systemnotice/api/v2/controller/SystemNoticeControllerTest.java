package net.causw.app.main.domain.community.systemnotice.api.v2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import net.causw.app.main.domain.community.systemnotice.api.v2.dto.response.SystemNoticeResponse;
import net.causw.app.main.domain.community.systemnotice.service.SystemNoticeService;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeResult;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

@ExtendWith(MockitoExtension.class)
class SystemNoticeControllerTest {

	@InjectMocks
	private SystemNoticeController systemNoticeController;

	@Mock
	private SystemNoticeService systemNoticeService;

	@Mock
	private CustomUserDetails userDetails;

	@Mock
	private User viewer;

	@Test
	void getLatestReturnsOkWithNullDataWhenThereIsNoSystemNotice() {
		given(userDetails.getUser()).willReturn(viewer);
		given(systemNoticeService.getLatest(viewer)).willReturn(Optional.empty());

		ResponseEntity<ApiResponse<SystemNoticeResponse>> response = systemNoticeController.getLatest(userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getData()).isNull();
	}

	@Test
	void getLatestReturnsMappedSystemNoticeWhenNoticeExists() {
		LocalDateTime createdAt = LocalDateTime.of(2026, 8, 12, 10, 0);
		SystemNoticeResult result = new SystemNoticeResult(
			"post-id", "테스트 제목", "content", "author", createdAt, true);
		given(userDetails.getUser()).willReturn(viewer);
		given(systemNoticeService.getLatest(viewer)).willReturn(Optional.of(result));

		ResponseEntity<ApiResponse<SystemNoticeResponse>> response = systemNoticeController.getLatest(userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		SystemNoticeResponse data = response.getBody().getData();
		assertThat(data).isNotNull();
		assertThat(data.id()).isEqualTo("post-id");
		assertThat(data.content()).isEqualTo("content");
		assertThat(data.authorName()).isEqualTo("author");
		assertThat(data.createdAt()).isEqualTo(createdAt);
		assertThat(data.isRead()).isTrue();
	}

	@Test
	void markAsReadDelegatesViewerAndPostIdToService() {
		given(userDetails.getUser()).willReturn(viewer);

		ApiResponse<Void> response = systemNoticeController.markAsRead("post-id", userDetails);

		verify(systemNoticeService).markAsRead(viewer, "post-id");
		assertThat(response).isNotNull();
		assertThat(response.getData()).isNull();
	}
}
