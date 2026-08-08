package net.causw.app.main.domain.community.systemnotice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.PostService;
import net.causw.app.main.domain.community.post.service.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.post.service.util.PostValidator;
import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateCommand;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeUpdateCommand;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeReader;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemNoticeService {

	private final SystemNoticeReader systemNoticeReader;
	private final SystemNoticeWriter systemNoticeWriter;
	private final PostService postService;
	private final BoardConfigReader boardConfigReader;

	@Transactional
	public SystemNoticeCreateResult create(SystemNoticeCreateCommand command) {
		String boardId = systemNoticeReader.getSystemNoticeBoardId();
		PostCreateResult result = postService.create(new PostCreateCommand(
			command.title(), command.content(), boardId, false, command.writer(), List.of(), List.of()));

		return new SystemNoticeCreateResult(
			result.id(), command.title(), result.content(), command.writer().getNickname(), result.createdAt());
	}

	@Transactional
	public void update(String postId, SystemNoticeUpdateCommand command) {
		systemNoticeReader.getSystemNoticePost(postId);
		postService.update(new PostUpdateCommand(
			postId, command.title(), command.content(), false, command.updater(), List.of(), List.of()));
	}

	@Transactional
	public void delete(String postId, User updater) {
		systemNoticeReader.getSystemNoticePost(postId);
		postService.deletePost(updater, postId);
	}

	public Optional<SystemNoticeResult> getLatest(User viewer) {
		CommunityPermissionPolicy.validateActiveUser(viewer);
		return systemNoticeReader.findLatestPost()
			.map(latestPost -> {
				validateReadAccess(viewer, latestPost);
				return toResult(latestPost, viewer);
			});
	}

	@Transactional
	public void markAsRead(User viewer, String postId) {
		CommunityPermissionPolicy.validateActiveUser(viewer);
		Post post = systemNoticeReader.findLatestPost()
			.filter(latestPost -> latestPost.getId().equals(postId))
			.orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
		validateReadAccess(viewer, post);

		UserSystemNoticeRead read = systemNoticeReader.findReadByUserId(viewer.getId())
			.orElseGet(() -> UserSystemNoticeRead.of(viewer.getId()));
		read.updateLastReadPost(post);
		systemNoticeWriter.save(read);
	}

	private void validateReadAccess(User viewer, Post post) {
		String boardId = post.getBoard().getId();
		BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(boardId);
		PostValidator.validateRead(viewer, post, boardConfig, boardAdminIds);
	}

	private SystemNoticeResult toResult(Post post, User viewer) {
		boolean isRead = systemNoticeReader.findReadByUserId(viewer.getId())
			.map(UserSystemNoticeRead::getLastReadPost)
			.map(Post::getId)
			.filter(post.getId()::equals)
			.isPresent();

		User writer = post.getWriter();
		String authorName = writer.getNickname();

		return new SystemNoticeResult(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			authorName,
			post.getCreatedAt(),
			isRead);
	}
}
