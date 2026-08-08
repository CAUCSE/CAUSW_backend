package net.causw.app.main.domain.community.systemnotice.api.v2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
	void getLatestReturnsNoContentWhenThereIsNoSystemNotice() {
		given(userDetails.getUser()).willReturn(viewer);
		given(systemNoticeService.getLatest(viewer)).willReturn(Optional.empty());

		ResponseEntity<ApiResponse<SystemNoticeResponse>> response =
			systemNoticeController.getLatest(userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNotNull();
	}
}
