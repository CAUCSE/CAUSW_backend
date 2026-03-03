package net.causw.app.main.domain.community.block.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.block.service.dto.ChildCommentBlockCreateCommand;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateResult;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockReader;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockWriter;
import net.causw.app.main.domain.user.relation.service.v2.util.BlockValidator;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.ChildCommentReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildCommentBlockService {

	private final BlockReader blockReader;
	private final BlockWriter blockWriter;
	private final UserReader userReader;
	private final ChildCommentReader childCommentReader;

	@Transactional
	public BlockCreateResult createBlock(ChildCommentBlockCreateCommand command) {
		User blocker = command.blocker();
		User blocked = userReader.findUserById(command.targetUserId());
		ChildComment childComment = childCommentReader.findByIdAndNotDeleted(command.childCommentId());

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, childComment.getIsAnonymous());

		// 익명 대댓글에서 이미 차단된 경우: 익명성 보호를 위해 성공 응답 반환
		if (childComment.getIsAnonymous() && alreadyBlocked) {
			return BlockCreateResult.builder()
				.blockedUserId(blocked.getId())
				.blockedUserName(blocked.getNickname())
				.build();
		}

		UserBlock userBlock = UserBlock.createForChildComment(
			blocker.getId(),
			blocked.getId(),
			childComment.getId(),
			childComment.getIsAnonymous(),
			childComment.getContent());
		UserBlock saved = blockWriter.save(userBlock);

		return BlockCreateResult.builder()
			.blockId(saved.getId())
			.blockedUserId(blocked.getId())
			.blockedUserName(blocked.getNickname())
			.createdAt(saved.getCreatedAt())
			.build();
	}
}
