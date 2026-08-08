package net.causw.app.main.domain.community.systemnotice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.PostService;
import net.causw.app.main.domain.community.post.service.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateCommand;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeUpdateCommand;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeReader;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeWriter;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemNoticeService {

	private final SystemNoticeReader systemNoticeReader;
	private final SystemNoticeWriter systemNoticeWriter;
	private final PostService postService;

	@Transactional
	public SystemNoticeCreateResult create(SystemNoticeCreateCommand command) {
		String boardId = systemNoticeReader.getSystemNoticeBoardId();
		PostCreateResult result = postService.create(new PostCreateCommand(
			command.content(), boardId, false, command.writer(), List.of(), List.of()));

		return new SystemNoticeCreateResult(
			result.id(), result.content(), command.writer().getNickname(), result.createdAt());
	}

	@Transactional
	public void update(String postId, SystemNoticeUpdateCommand command) {
		systemNoticeReader.getSystemNoticePost(postId);
		postService.update(new PostUpdateCommand(
			postId, command.content(), false, command.updater(), List.of(), List.of()));
	}

	@Transactional
	public void delete(String postId, User updater) {
		systemNoticeReader.getSystemNoticePost(postId);
		postService.deletePost(updater, postId);
	}

	public Optional<SystemNoticeResult> getLatest(User viewer) {
		return systemNoticeReader.findLatestPost()
			.map(latestPost -> toResult(latestPost, viewer));
	}

	@Transactional
	public void markAsRead(User viewer, String postId) {
		Post post = systemNoticeReader.getSystemNoticePost(postId);

		UserSystemNoticeRead read = systemNoticeReader.findReadByUserId(viewer.getId())
			.orElseGet(() -> UserSystemNoticeRead.of(viewer.getId()));
		read.updateLastReadPost(post);
		systemNoticeWriter.save(read);
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
			null,
			post.getContent(),
			authorName,
			post.getCreatedAt(),
			isRead);
	}
}
