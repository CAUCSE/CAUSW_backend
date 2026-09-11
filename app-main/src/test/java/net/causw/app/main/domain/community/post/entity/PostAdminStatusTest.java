package net.causw.app.main.domain.community.post.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.user.account.entity.user.User;

class PostAdminStatusTest {
	@Test
	@DisplayName("숨김 상태는 삭제 상태와 구분된다")
	void changeAdminStatus_shouldDistinguishHiddenFromDeleted() {
		// given
		Post post = Post.of("제목", "내용", mock(User.class), false, mock(Board.class), List.of());

		// when
		post.changeAdminStatus(PostAdminStatus.HIDDEN);

		// then
		assertThat(post.getIsHidden()).isTrue();
		assertThat(post.getIsDeleted()).isFalse();
		assertThat(PostAdminStatus.from(post)).isEqualTo(PostAdminStatus.HIDDEN);
	}

	@Test
	@DisplayName("삭제된 게시물을 공개하면 삭제와 숨김 상태가 모두 해제된다")
	void changeAdminStatus_shouldRestoreDeletedPost() {
		// given
		Post post = Post.of("제목", "내용", mock(User.class), false, mock(Board.class), List.of());
		post.changeAdminStatus(PostAdminStatus.DELETED);

		// when
		post.changeAdminStatus(PostAdminStatus.VISIBLE);

		// then
		assertThat(post.getIsHidden()).isFalse();
		assertThat(post.getIsDeleted()).isFalse();
	}
}
