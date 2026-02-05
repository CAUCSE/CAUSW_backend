package net.causw.app.main.domain.community.post.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.community.form.entity.Form;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * Post 도메인의 쓰기 작업을 담당하는 컴포넌트
 * Repository를 통해 데이터를 생성, 수정, 삭제합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class PostWriter {
	private final PostRepository postRepository;

	/**
	 * 새로운 Post를 저장합니다.
	 *
	 * @param post Post Entity
	 * @return 저장된 Post Entity
	 */
	public Post save(Post post) {
		return postRepository.save(post);
	}

	/**
	 * Post를 업데이트합니다.
	 *
	 * @param post 수정할 Post
	 * @param title 제목
	 * @param content 내용
	 * @param form Form Entity
	 * @param postAttachImageList 첨부 이미지 리스트
	 * @return 수정된 Post Entity
	 */
	public Post update(Post post, String title, String content, Form form, List<PostAttachImage> postAttachImageList) {
		post.update(title, content, form, postAttachImageList);
		return postRepository.save(post);
	}

	/**
	 * Post에 Vote를 연결합니다.
	 *
	 * @param post Post Entity
	 * @param vote Vote Entity
	 * @return 수정된 Post Entity
	 */
	public Post updateVote(Post post, Vote vote) {
		post.updateVote(vote);
		return postRepository.save(post);
	}

	/**
	 * Post를 삭제 상태로 변경합니다 (Soft Delete).
	 *
	 * @param postId 삭제할 Post
	 */
	public void deleteById(String postId) {
		Post post = postRepository.findById(postId).orElseThrow(
			PostErrorCode.POST_NOT_FOUND::toBaseException);
		post.setIsDeleted(true);
		postRepository.save(post);
	}

	/**
	 * Post의 삭제 상태를 변경합니다 (Soft Delete).
	 *
	 * @param postId 삭제할 PostId
	 * @param isDeleted 변경할 삭제 상태
	 */
	public void setIsDeletedById(String postId, Boolean isDeleted) {
		Post post = postRepository.findById(postId).orElseThrow(
			PostErrorCode.POST_NOT_FOUND::toBaseException);
		post.setIsDeleted(isDeleted);
		postRepository.save(post);
	}

	/**
	 * 특정 게시판의 모든 게시글을 삭제 상태로 변경합니다 (Soft Delete).
	 *
	 * @param boardId Board ID
	 * @return 삭제된 게시글 개수
	 */
	public int deleteAllByBoardId(String boardId) {
		return postRepository.deleteAllPostsByBoardId(boardId);
	}

	/**
	 * Post를 물리적으로 삭제합니다 (Hard Delete).
	 * 주의: 이 작업은 되돌릴 수 없습니다.
	 *
	 * @param postId 삭제할 PostId
	 */
	public void hardDeleteById(String postId) {
		postRepository.deleteById(postId);
	}
}
