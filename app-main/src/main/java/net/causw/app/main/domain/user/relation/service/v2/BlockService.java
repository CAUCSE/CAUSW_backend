package net.causw.app.main.domain.user.relation.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.block.service.dto.ChildCommentBlockCreateCommand;
import net.causw.app.main.domain.community.block.service.dto.CommentBlockCreateCommand;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.v2.implementation.CommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateCommand;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateResult;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockReader;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockWriter;
import net.causw.app.main.domain.user.relation.service.v2.util.BlockValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

	private final BlockReader blockReader;
	private final BlockWriter blockWriter;
	private final PostReader postReader;
	private final CommentReader commentReader;
	private final ChildCommentReader childCommentReader;

	@Transactional
	public BlockCreateResult createBlockByPost(BlockCreateCommand command) {
		User blocker = command.blocker();
		Post post = postReader.findByIdAndNotDeleted(command.postId());
		User blocked = post.getWriter();

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, post.getIsAnonymous());

		if (post.getIsAnonymous() && alreadyBlocked) {
			return BlockCreateResult.builder().build();
		}

		UserBlock userBlock = UserBlock.createForPost(
			blocker.getId(),
			blocked.getId(),
			post.getId(),
			post.getIsAnonymous(),
			post.getContent());
		UserBlock saved = blockWriter.save(userBlock);

		if (post.getIsAnonymous()) {
			return BlockCreateResult.builder()
				.blockId(saved.getId())
				.createdAt(saved.getCreatedAt())
				.build();
		}

		return BlockCreateResult.builder()
			.blockId(saved.getId())
			.blockedUserId(blocked.getId())
			.blockedUserName(blocked.getNickname())
			.createdAt(saved.getCreatedAt())
			.build();
	}

	@Transactional
	public BlockCreateResult createBlockByComment(CommentBlockCreateCommand command) {
		User blocker = command.blocker();
		Comment comment = commentReader.findByIdAndNotDeleted(command.commentId());
		User blocked = comment.getWriter();

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, comment.getIsAnonymous());

		if (comment.getIsAnonymous() && alreadyBlocked) {
			return BlockCreateResult.builder().build();
		}

		UserBlock userBlock = UserBlock.createForComment(
			blocker.getId(),
			blocked.getId(),
			comment.getId(),
			comment.getIsAnonymous(),
			comment.getContent());
		UserBlock saved = blockWriter.save(userBlock);

		if (comment.getIsAnonymous()) {
			return BlockCreateResult.builder()
				.blockId(saved.getId())
				.createdAt(saved.getCreatedAt())
				.build();
		}

		return BlockCreateResult.builder()
			.blockId(saved.getId())
			.blockedUserId(blocked.getId())
			.blockedUserName(blocked.getNickname())
			.createdAt(saved.getCreatedAt())
			.build();
	}

	@Transactional
	public BlockCreateResult createBlockByChildComment(ChildCommentBlockCreateCommand command) {
		User blocker = command.blocker();
		ChildComment childComment = childCommentReader.findByIdAndNotDeleted(command.childCommentId());
		User blocked = childComment.getWriter();

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, childComment.getIsAnonymous());

		if (childComment.getIsAnonymous() && alreadyBlocked) {
			return BlockCreateResult.builder().build();
		}

		UserBlock userBlock = UserBlock.createForChildComment(
			blocker.getId(),
			blocked.getId(),
			childComment.getId(),
			childComment.getIsAnonymous(),
			childComment.getContent());
		UserBlock saved = blockWriter.save(userBlock);

		if (childComment.getIsAnonymous()) {
			return BlockCreateResult.builder()
				.blockId(saved.getId())
				.createdAt(saved.getCreatedAt())
				.build();
		}

		return BlockCreateResult.builder()
			.blockId(saved.getId())
			.blockedUserId(blocked.getId())
			.blockedUserName(blocked.getNickname())
			.createdAt(saved.getCreatedAt())
			.build();
	}
}
