package net.causw.app.main.domain.community.block.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.block.service.dto.CommentBlockCreateCommand;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.CommentReader;
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
public class CommentBlockService {

	private final BlockReader blockReader;
	private final BlockWriter blockWriter;
	private final CommentReader commentReader;

	@Transactional
	public BlockCreateResult createBlock(CommentBlockCreateCommand command) {
		User blocker = command.blocker();
		Comment comment = commentReader.findByIdAndNotDeleted(command.commentId());
		User blocked = comment.getWriter(); // 댓글 작성자를 서버에서 직접 도출

		boolean alreadyBlocked = blockReader.existsByBlockerAndBlocked(blocker, blocked);
		BlockValidator.validateCreate(blocker, blocked, alreadyBlocked, comment.getIsAnonymous());

		// 익명 댓글 + 이미 차단: 익명성 보호를 위해 신원 없이 성공 응답 반환
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

		// 익명 댓글이면 응답에서 신원 정보 제외
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
}
