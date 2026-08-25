package net.causw.app.main.domain.community.systemnotice.service.implementation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardConfigRepository;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.systemnotice.repository.UserSystemNoticeReadRepository;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

@ExtendWith(MockitoExtension.class)
class SystemNoticeReaderTest {

	@InjectMocks
	private SystemNoticeReader systemNoticeReader;

	@Mock
	private BoardConfigRepository boardConfigRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private UserSystemNoticeReadRepository userSystemNoticeReadRepository;

	@Test
	void givenMultipleSystemNoticeBoards_whenGetBoardId_thenThrowsDomainException() {
		BoardConfig firstConfig = org.mockito.Mockito.mock(BoardConfig.class);
		BoardConfig secondConfig = org.mockito.Mockito.mock(BoardConfig.class);
		given(boardConfigRepository.findByIsSystemNoticeTrue())
			.willReturn(List.of(firstConfig, secondConfig));

		assertThatThrownBy(systemNoticeReader::getSystemNoticeBoardId)
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.BOARD_SYSTEM_NOTICE_NOT_UNIQUE);
	}
}
