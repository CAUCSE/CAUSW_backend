package net.causw.app.main.domain.community.systemnotice.service.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.repository.BoardConfigRepository;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;
import net.causw.app.main.domain.community.systemnotice.repository.UserSystemNoticeReadRepository;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemNoticeReader {

	private final BoardConfigRepository boardConfigRepository;
	private final PostRepository postRepository;
	private final UserSystemNoticeReadRepository userSystemNoticeReadRepository;

	public Optional<Post> findLatestPost() {
		return findSystemNoticeConfig()
			.flatMap(config -> postRepository
				.findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(config.getBoardId()));
	}

	public List<Post> findAllPosts() {
		return findSystemNoticeConfig()
			.map(config -> postRepository
				.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(config.getBoardId()))
			.orElseGet(List::of);
	}

	public Optional<UserSystemNoticeRead> findReadByUserId(String userId) {
		return userSystemNoticeReadRepository.findByUserId(userId);
	}

	public Post getSystemNoticePost(String postId) {
		return postRepository.findByIdAndIsDeletedFalse(postId)
			.filter(this::isSystemNoticePost)
			.orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
	}

	public String getSystemNoticeBoardId() {
		return findSystemNoticeConfig()
			.map(BoardConfig::getBoardId)
			.orElseThrow(BoardErrorCode.BOARD_NOT_FOUND::toBaseException);
	}

	private Optional<BoardConfig> findSystemNoticeConfig() {
		List<BoardConfig> systemNoticeConfigs = boardConfigRepository.findByIsSystemNoticeTrue();
		if (systemNoticeConfigs.size() > 1) {
			throw BoardErrorCode.BOARD_SYSTEM_NOTICE_NOT_UNIQUE.toBaseException();
		}
		return systemNoticeConfigs.stream().findFirst();
	}

	private boolean isSystemNoticePost(Post post) {
		return findSystemNoticeConfig()
			.map(BoardConfig::getBoardId)
			.filter(post.getBoard().getId()::equals)
			.isPresent();
	}
}
