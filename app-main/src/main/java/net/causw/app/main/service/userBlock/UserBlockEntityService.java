package net.causw.app.main.service.userBlock;

import java.util.Set;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userBlock.UserBlock;
import net.causw.app.main.repository.userBlock.UserBlockRepository;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBlockEntityService {

	private final UserBlockRepository userBlockRepository;

	/**
	 * 차단자 - 피차단자 Id 쌍으로 활성화된 차단이 있는지 확인하는 메서드
	 * @param blocker
	 * @param blockee
	 * @return
	 */
	public boolean existsBlockByUsers(User blocker, User blockee) {
		return userBlockRepository.existsByBlockerIdAndBlockeeId(blocker.getId(), blockee.getId());
	}

	/**
	 * 게시물을 통한 유저 차단
	 * @param user 차단 시도자
	 * @param post 차단 대상 게시물
	 */
	public void createBlock(User user, Post post) {
		User writer = post.getWriter();

		UserBlock userBlock = UserBlock.createForPost(
			user.getId(),
			writer.getId(),
			post.getId(),
			post.getIsAnonymous(),
			post.getTitle() + "\n내용: " + post.getContent()
		);

		userBlockRepository.save(userBlock);
	}

	/**
	 * 댓글을 통한 유저 차단
	 * @param user 차단 시도자
	 * @param comment 차단 대상 댓글
	 */
	public void createBlock(User user, Comment comment) {
		User writer = comment.getWriter();

		UserBlock userBlock = UserBlock.createForComment(
			user.getId(),
			writer.getId(),
			comment.getId(),
			comment.getIsAnonymous(),
			comment.getContent()
		);

		userBlockRepository.save(userBlock);
	}

	/**
	 * 대댓글을 통한 유저 차단
	 * @param user 차단 시도자
	 * @param childComment 차단 대상 대댓글
	 */
	public void createBlock(User user, ChildComment childComment) {
		User writer = childComment.getWriter();

		UserBlock userBlock = UserBlock.createForChildComment(
			user.getId(),
			writer.getId(),
			childComment.getId(),
			childComment.getIsAnonymous(),
			childComment.getContent()
		);

		userBlockRepository.save(userBlock);
	}

	/**
	 * 차단당한 유저의 아이디를 가져오는 메서드
	 * @param blocker 차단자
	 * @return 피차단자 id Set
	 */
	public Set<String> findBlockeeUserIdsByBlocker(User blocker) {
		return userBlockRepository.findBlockeeIdsByBlockerUserId(blocker.getId());
	}

	/**
	 * 차단을 한 유저의 아이디를 가져오는 메서드
	 * @param blockee 차단당한 자
	 * @return 차단자 id Set
	 */
	public Set<String> findBlockerUserIdsByBlockee(User blockee) {
		return userBlockRepository.findBlockerIdsByBlockeeUserId(blockee.getId());
	}

	/**
	 * 피차단자 ID 목록으로 차단자 ID 목록을 조회하는 메서드
	 * @param blockeeUserIds 차단당한 자들의 아이디 Set
	 * @return 차단자 id Set
	 */
	public Set<String> findBlockerUserIdsByUserIds(@NotEmpty Set<String> blockeeUserIds) {
		return userBlockRepository.findBlockerIdsByBlockeeUserIds(blockeeUserIds);
	}
}
