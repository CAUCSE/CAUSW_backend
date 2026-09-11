package net.causw.app.main.domain.community.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Map;

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
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostAdminStatus;
import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.service.dto.PostAdminDetailResult;
import net.causw.app.main.domain.community.post.service.dto.PostAdminListQuery;
import net.causw.app.main.domain.community.post.service.dto.PostAdminSummaryResult;
import net.causw.app.main.domain.community.post.service.dto.UncategorizedPostResult;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
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

	@Mock
	private PostWriter postWriter;

	@Mock
	private LikePostReader likePostReader;

	@Mock
	private CommentReader commentReader;

	private String postId;
	private Post post;

	@BeforeEach
	void setUp() {
		postId = "post-id";
		User writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		Board board = ObjectFixtures.getBoardV2WithId("board-id");
		post = ObjectFixtures.getPost(writer, board);
		ReflectionTestUtils.setField(post, "id", postId);
	}

	@Test
	@DisplayName("관리자가 지정한 성격으로 덮어쓴다")
	void updateCategory_shouldOverwriteCategory() {
		// given
		post.setCrawled();
		post.updateCategory(PostCategory.ACADEMIC);
		given(postReader.findByIdAndNotDeletedIncludingHidden(postId)).willReturn(post);

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
		given(postReader.findByIdAndNotDeletedIncludingHidden(postId)).willReturn(post);

		// when
		postAdminService.updateCategory(postId, null);

		// then
		assertThat(post.getCategory()).isNull();
	}

	@Test
	@DisplayName("크롤링 게시글이 아니면 수정할 수 없다")
	void updateCategory_shouldRejectNonCrawledPost() {
		// given
		given(postReader.findByIdAndNotDeletedIncludingHidden(postId)).willReturn(post);

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

	@Test
	@DisplayName("5개 분류에 해당하지 않는 게시글은 기타로 지정할 수 있다")
	void updateCategory_shouldAllowEtc() {
		// given
		post.setCrawled();
		given(postReader.findByIdAndNotDeletedIncludingHidden(postId)).willReturn(post);

		// when
		postAdminService.updateCategory(postId, PostCategory.ETC);

		// then
		assertThat(post.getCategory()).isEqualTo(PostCategory.ETC);
	}

	@Test
	@DisplayName("관리자 목록 조회 시 게시물 성격을 포함한 검색 조건을 전달한다")
	void getPosts_shouldPassCategoryCondition() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		PostAdminListQuery query = new PostAdminListQuery(
			"board-id", PostCategory.RECRUIT, PostAdminStatus.VISIBLE, "검색", "작성자");
		given(postReader.findAllForAdmin(query, pageable)).willReturn(Page.empty(pageable));

		// when
		Page<PostAdminSummaryResult> result = postAdminService.getPosts(query, pageable);

		// then
		assertThat(result).isEmpty();
		then(postReader).should().findAllForAdmin(query, pageable);
	}

	@Test
	@DisplayName("관리자 게시물 목록 결과에 성격과 집계 정보를 포함한다")
	void getPosts_shouldContainCategoryAndEngagementCounts() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		PostAdminListQuery query = new PostAdminListQuery(null, null, null, null, null);
		post.updateCategory(PostCategory.EVENT_LECTURE);
		given(postReader.findAllForAdmin(query, pageable)).willReturn(new PageImpl<>(List.of(post), pageable, 1));
		given(commentReader.countByPostIds(List.of(post.getId()))).willReturn(Map.of(post.getId(), 4L));
		given(likePostReader.countByPostIds(List.of(post.getId()))).willReturn(Map.of(post.getId(), 5L));

		// when
		PostAdminSummaryResult result = postAdminService.getPosts(query, pageable).getContent().get(0);

		// then
		assertThat(result.category()).isEqualTo(PostCategory.EVENT_LECTURE);
		assertThat(result.commentCount()).isEqualTo(4L);
		assertThat(result.likeCount()).isEqualTo(5L);
	}

	@Test
	@DisplayName("관리자가 게시물을 숨김 처리할 수 있다")
	void changeStatus_shouldDelegateHiddenStatus() {
		// given
		given(postReader.findById(postId)).willReturn(post);

		// when
		postAdminService.changeStatus(postId, PostAdminStatus.HIDDEN);

		// then
		then(postWriter).should().changeAdminStatus(post, PostAdminStatus.HIDDEN);
	}

	@Test
	@DisplayName("관리자 게시물 상세 응답에 게시물 성격을 포함한다")
	void getPostDetail_shouldContainCategory() {
		// given
		post.updateCategory(PostCategory.RESEARCH);
		given(postReader.findById(postId)).willReturn(post);
		given(postReader.findPostImages(postId)).willReturn(List.of());
		given(commentReader.countByPostId(postId)).willReturn(2L);
		given(likePostReader.countByPostId(postId)).willReturn(3L);

		// when
		PostAdminDetailResult result = postAdminService.getPostDetail(postId);

		// then
		assertThat(result.category()).isEqualTo(PostCategory.RESEARCH);
		assertThat(result.commentCount()).isEqualTo(2L);
		assertThat(result.likeCount()).isEqualTo(3L);
	}
}
