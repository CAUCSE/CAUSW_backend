package net.causw.app.main.domain.user.relation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;
import net.causw.app.main.domain.user.relation.service.dto.ChildCommentBlockCreateCommand;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;
import net.causw.app.main.domain.user.relation.service.implementation.BlockWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

	@InjectMocks
	private BlockService blockService;

	@Mock
	private BlockReader blockReader;

	@Mock
	private BlockWriter blockWriter;

	@Mock
	private PostReader postReader;

	@Mock
	private CommentReader commentReader;

	@Test
	@DisplayName("대댓글 차단 대상이 루트 댓글이면 예외가 발생한다")
	void createBlockByChildComment_shouldFail_whenCommentIsRoot() {
		// given
		User blocker = mock(User.class);
		Comment rootComment = mock(Comment.class);
		ChildCommentBlockCreateCommand command = new ChildCommentBlockCreateCommand("root-comment-id", blocker);

		given(commentReader.getComment("root-comment-id")).willReturn(rootComment);
		given(rootComment.isChildComment()).willReturn(false);

		// when & then
		assertThatThrownBy(() -> blockService.createBlockByChildComment(command))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND);

		verify(blockWriter, never()).save(org.mockito.ArgumentMatchers.any(UserBlock.class));
	}
}
