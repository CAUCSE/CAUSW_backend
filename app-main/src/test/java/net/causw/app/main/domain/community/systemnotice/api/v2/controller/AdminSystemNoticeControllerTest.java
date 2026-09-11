package net.causw.app.main.domain.community.systemnotice.api.v2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.systemnotice.api.v2.dto.response.SystemNoticeCreateResponse;
import net.causw.app.main.domain.community.systemnotice.api.v2.mapper.SystemNoticeDtoMapper;
import net.causw.app.main.domain.community.systemnotice.service.SystemNoticeService;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

@ExtendWith(MockitoExtension.class)
class AdminSystemNoticeControllerTest {

	@InjectMocks
	private AdminSystemNoticeController adminSystemNoticeController;

	@Mock
	private SystemNoticeService systemNoticeService;
	@Mock
	private SystemNoticeDtoMapper systemNoticeDtoMapper;
	@Mock
	private CustomUserDetails userDetails;

	@Test
	void getAllReturnsMappedSystemNotices() {
		SystemNoticeCreateResult result = new SystemNoticeCreateResult(
			"post-id", "공지", "내용", "관리자", LocalDateTime.of(2026, 8, 25, 12, 0));
		SystemNoticeCreateResponse response = new SystemNoticeCreateResponse(
			"post-id", "공지", "내용", "관리자", LocalDateTime.of(2026, 8, 25, 12, 0));
		given(systemNoticeService.getAll()).willReturn(List.of(result));
		given(systemNoticeDtoMapper.toCreateResponseList(List.of(result))).willReturn(List.of(response));

		ApiResponse<List<SystemNoticeCreateResponse>> apiResponse = adminSystemNoticeController.getAll(userDetails);

		assertThat(apiResponse.getData()).containsExactly(response);
	}
}
