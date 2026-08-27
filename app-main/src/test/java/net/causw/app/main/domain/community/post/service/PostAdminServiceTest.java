package net.causw.app.main.domain.community.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.service.dto.UncategorizedPostResult;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostAdminService 테스트")
class PostAdminServiceTest {

	@InjectMocks
	private PostAdminService postAdminService;

	@Mock
	private PostReader postReader;

	private String postId;
	private Post post;

	@BeforeEach
	void setUp() {
		postId = "post-id";
		User writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		Board board = ObjectFixtures.getBoardV2WithId("board-id");
		post = ObjectFixtures.getPost(writer, board);
	}

	@Test
	@DisplayName("관리자가 지정한 성격으로 덮어쓴다")
	void updateCategory_shouldOverwriteCategory() {
		// given
		post.setCrawled();
		post.updateCategory(PostCategory.ACADEMIC);
		given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);

		// when
		postAdminService.updateCategory(postId, PostCategory.RECRUIT);

		// then
		assertThat(post.getCategory()).isEqualTo(PostCategory.RECRUIT);
	}

	@Test
	@DisplayName("null을 전달하면 미분류로 되돌린다")
	void updateCategory_shouldResetToUnclassified() {
		// given
		post.setCrawled();
		post.updateCategory(PostCategory.ACADEMIC);
		given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);

		// when
		postAdminService.updateCategory(postId, null);

		// then
		assertThat(post.getCategory()).isNull();
	}

	@Test
	@DisplayName("크롤링 게시글이 아니면 수정할 수 없다")
	void updateCategory_shouldRejectNonCrawledPost() {
		// given
		given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);

		// when & then
		assertThatThrownBy(() -> postAdminService.updateCategory(postId, PostCategory.RECRUIT))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.satisfies(ex -> assertThat(((BaseRunTimeV2Exception)ex).getErrorCode())
				.isEqualTo(PostErrorCode.POST_CATEGORY_NOT_SUPPORTED));
	}

	@Test
	@DisplayName("성격이 미분류인 크롤링 게시글 목록을 조회한다")
	void getUncategorizedPosts_shouldReturnUncategorizedCrawledPosts() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		post.setCrawled();
		given(postReader.findUncategorizedCrawledPosts(pageable))
			.willReturn(new PageImpl<>(List.of(post), pageable, 1));

		// when
		Page<UncategorizedPostResult> result = postAdminService.getUncategorizedPosts(pageable);

		// then
		assertThat(result.getContent()).singleElement()
			.satisfies(item -> assertThat(item.title()).isEqualTo(post.getTitle()));
		assertThat(result.getTotalElements()).isEqualTo(1);
	}
}
