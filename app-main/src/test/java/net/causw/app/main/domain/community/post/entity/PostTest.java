package net.causw.app.main.domain.community.post.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.form.entity.Form;
import net.causw.app.main.util.ObjectFixtures;

class PostTest {

	@Test
	@DisplayName("게시글 삭제 상태는 연결된 폼에 전파되고 null은 미삭제로 정규화된다")
	void setIsDeleted_shouldCascadeToFormAndNormalizeNull() {
		Form form = Form.createPostForm(
			"폼", false, List.of(), false, false, List.of(), false, List.of());
		Post post = Post.of(
			"제목",
			"내용",
			ObjectFixtures.getCertifiedUser(),
			false,
			false,
			ObjectFixtures.getBoardV2WithId("board-id"),
			form,
			List.of());

		post.setIsDeleted(true);

		assertThat(post.getIsDeleted()).isTrue();
		assertThat(form.getIsDeleted()).isTrue();

		post.setIsDeleted(null);

		assertThat(post.getIsDeleted()).isFalse();
		assertThat(form.getIsDeleted()).isFalse();
	}
}
