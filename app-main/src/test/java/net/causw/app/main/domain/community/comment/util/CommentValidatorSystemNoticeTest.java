package net.causw.app.main.domain.community.comment.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;

@ExtendWith(MockitoExtension.class)
class CommentValidatorSystemNoticeTest {

	@InjectMocks
	private CommentValidator commentValidator;

	@Mock
	private LikeCommentReader likeCommentReader;

	@Mock
	private User creator;

	@Mock
	private Post post;

	@Mock
	private BoardConfig boardConfig;

	@Test
	void validateForCreateRejectsCommentOnSystemNotice() {
		given(boardConfig.isSystemNotice()).willReturn(true);

		assertThatThrownBy(() -> commentValidator.validateForCreate(creator, post, boardConfig, List.of()))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(CommentErrorCode.COMMENT_NOT_ALLOWED_ON_SYSTEM_NOTICE);
	}
}
