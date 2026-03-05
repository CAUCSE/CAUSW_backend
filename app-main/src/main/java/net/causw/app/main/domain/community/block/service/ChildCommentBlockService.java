package net.causw.app.main.domain.community.block.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.block.service.dto.ChildCommentBlockCreateCommand;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.ChildCommentReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateResult;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockReader;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockWriter;
import net.causw.app.main.domain.user.relation.service.v2.util.BlockValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildCommentBlockService {

	private final BlockReader blockReader;
	private final BlockWriter blockWriter;
	private final ChildCommentReader childCommentReader;

	@Transactional
	public BlockCreateResult createBlock(ChildCommentBlockCreateCommand command) {
		User blocker = command.blocker();
		ChildComment childComment = childCommentReader.findByIdAndNotDeleted(command.childCommentId());
		User blocked = childComment.getWriter(); // 대댓글 작성자를 서버에서 직접 도출

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, childComment.getIsAnonymous());

		// 익명 대댓글 + 이미 차단: 익명성 보호를 위해 신원 없이 성공 응답 반환
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

		// 익명 대댓글이면 응답에서 신원 정보 제외
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
