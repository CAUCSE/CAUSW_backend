package net.causw.app.main.domain.community.block.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.block.service.dto.BlockCreateResult;
import net.causw.app.main.domain.community.block.service.dto.CommentBlockCreateCommand;
import net.causw.app.main.domain.community.block.service.implementation.BlockReader;
import net.causw.app.main.domain.community.block.service.implementation.BlockWriter;
import net.causw.app.main.domain.community.block.service.util.BlockValidator;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.CommentReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentBlockService {

	private final BlockReader blockReader;
	private final BlockWriter blockWriter;
	private final UserReader userReader;
	private final CommentReader commentReader;

	@Transactional
	public BlockCreateResult createBlock(CommentBlockCreateCommand command) {
		User blocker = command.blocker();
		User blocked = userReader.findUserById(command.targetUserId());
		Comment comment = commentReader.findByIdAndNotDeleted(command.commentId());

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, comment.getIsAnonymous());

		// 익명 댓글에서 이미 차단된 경우: 익명성 보호를 위해 성공 응답 반환
		if (comment.getIsAnonymous() && alreadyBlocked) {
			return BlockCreateResult.builder()
				.blockedUserId(blocked.getId())
				.blockedUserName(blocked.getNickname())
				.build();
		}

		UserBlock userBlock = UserBlock.createForComment(
			blocker.getId(),
			blocked.getId(),
			comment.getId(),
			comment.getIsAnonymous(),
			comment.getContent());
		UserBlock saved = blockWriter.save(userBlock);

		return BlockCreateResult.builder()
			.blockId(saved.getId())
			.blockedUserId(blocked.getId())
			.blockedUserName(blocked.getNickname())
			.createdAt(saved.getCreatedAt())
			.build();
	}
}
