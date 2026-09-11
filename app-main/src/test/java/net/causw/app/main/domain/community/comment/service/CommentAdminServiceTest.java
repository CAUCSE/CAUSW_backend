package net.causw.app.main.domain.community.comment.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.enums.CommentAdminStatus;
import net.causw.app.main.domain.community.comment.service.implementation.CommentAdminWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

	@InjectMocks
	CommentAdminService commentAdminService;

	@Mock
	CommentReader commentReader;

	@Mock
	CommentAdminWriter commentAdminWriter;

	@Test
	@DisplayName("관리자가 삭제된 댓글을 공개 상태로 복구할 수 있다")
	void changeStatus_shouldRestoreDeletedComment() {
		// given
		Comment comment = mock(Comment.class);
		given(commentReader.getCommentIncludeDeleted("comment-id")).willReturn(comment);

		// when
		commentAdminService.changeStatus("comment-id", CommentAdminStatus.VISIBLE);

		// then
		then(commentAdminWriter).should().changeStatus(comment, CommentAdminStatus.VISIBLE);
	}
}
