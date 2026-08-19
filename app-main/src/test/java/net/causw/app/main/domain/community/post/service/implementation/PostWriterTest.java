package net.causw.app.main.domain.community.post.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.form.repository.FormRepository;
import net.causw.app.main.domain.community.post.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostWriterTest {

	@InjectMocks
	private PostWriter postWriter;

	@Mock
	private PostRepository postRepository;

	@Mock
	private FormRepository formRepository;

	@Test
	@DisplayName("게시판 삭제 시 연결된 폼을 먼저 삭제하고 게시글을 삭제한다")
	void deleteAllByBoardId_shouldSoftDeleteFormsBeforePosts() {
		given(formRepository.softDeleteAllByBoardId("board-id")).willReturn(2);
		given(postRepository.deleteAllPostsByBoardId("board-id")).willReturn(3);

		int deletedPostCount = postWriter.deleteAllByBoardId("board-id");

		assertThat(deletedPostCount).isEqualTo(3);
		InOrder inOrder = inOrder(formRepository, postRepository);
		inOrder.verify(formRepository).softDeleteAllByBoardId("board-id");
		inOrder.verify(postRepository).deleteAllPostsByBoardId("board-id");
	}
}
