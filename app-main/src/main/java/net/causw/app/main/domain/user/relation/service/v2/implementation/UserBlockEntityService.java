package net.causw.app.main.domain.user.relation.service.v2.implementation;

import java.util.Set;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;
import net.causw.app.main.domain.user.relation.relation.userBlock.UserBlockRepository;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBlockEntityService {

	private final UserBlockRepository userBlockRepository;

	public boolean existsBlockByUsers(User blocker, User blockee) {
		return userBlockRepository.existsByBlockerIdAndBlockeeId(blocker.getId(), blockee.getId());
	}

	public void createBlock(User user, Post post) {
		User writer = post.getWriter();

		UserBlock userBlock = UserBlock.createForPost(
			user.getId(),
			writer.getId(),
			post.getId(),
			post.getIsAnonymous(),
			post.getTitle() + "\n내용: " + post.getContent());

		userBlockRepository.save(userBlock);
	}

	public void createBlock(User user, Comment comment) {
		User writer = comment.getWriter();

		UserBlock userBlock = UserBlock.createForComment(
			user.getId(),
			writer.getId(),
			comment.getId(),
			comment.getIsAnonymous(),
			comment.getContent());

		userBlockRepository.save(userBlock);
	}

	public void createBlock(User user, ChildComment childComment) {
		User writer = childComment.getWriter();

		UserBlock userBlock = UserBlock.createForChildComment(
			user.getId(),
			writer.getId(),
			childComment.getId(),
			childComment.getIsAnonymous(),
			childComment.getContent());

		userBlockRepository.save(userBlock);
	}

	public Set<String> findBlockeeUserIdsByBlocker(User blocker) {
		return userBlockRepository.findBlockeeIdsByBlockerUserId(blocker.getId());
	}

	public Set<String> findBlockerUserIdsByBlockee(User blockee) {
		return userBlockRepository.findBlockerIdsByBlockeeUserId(blockee.getId());
	}

	public Set<String> findBlockerUserIdsByUserIds(@NotEmpty Set<String> blockeeUserIds) {
		return userBlockRepository.findBlockerIdsByBlockeeUserIds(blockeeUserIds);
	}
}
