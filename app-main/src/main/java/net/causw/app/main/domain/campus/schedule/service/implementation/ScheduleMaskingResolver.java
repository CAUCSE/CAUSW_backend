package net.causw.app.main.domain.campus.schedule.service.implementation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.util.PostValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;

import lombok.RequiredArgsConstructor;

/**
 * 일정 DTO의 targetPostId에 대한 읽기 권한을 배치로 검증하고,
 * 권한이 없는 항목을 마스킹하는 책임을 담당하는 Implementation 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleMaskingResolver {

	private final PostReader postReader;
	private final BoardConfigReader boardConfigReader;

	/**
	 * 일정 DTO 목록에서 viewer가 읽을 수 있는 게시글 ID 집합을 배치 조회하여 반환합니다.
	 * viewer가 null이거나 targetPostId가 없는 경우 빈 집합을 반환합니다.
	 *
	 * @param scheduleDtos 일정 DTO 목록
	 * @param viewer       조회 요청 사용자
	 * @return viewer가 읽기 가능한 게시글 ID 집합
	 */
	public Set<String> resolveReadablePostIds(List<ScheduleDto> scheduleDtos, User viewer) {
		if (viewer == null) {
			return Set.of();
		}

		Set<String> targetPostIds = extractTargetPostIds(scheduleDtos);
		if (targetPostIds.isEmpty()) {
			return Set.of();
		}

		Map<String, Post> postsById = postReader.findPostMapByIds(targetPostIds);
		Set<String> boardIds = extractActiveBoardIds(postsById);
		if (boardIds.isEmpty()) {
			return Set.of();
		}

		Map<String, BoardConfig> boardConfigMap = boardConfigReader.getBoardConfigMapByBoardIds(List.copyOf(boardIds));
		Map<String, Set<String>> boardAdminMap = boardConfigReader.getAdminIdSetMapByBoardIds(boardIds);

		return filterReadablePostIds(postsById.values(), viewer, boardConfigMap, boardAdminMap);
	}

	/**
	 * viewer가 targetPostId를 읽을 수 없는 경우 targetPostId를 null로 마스킹한 ScheduleDto를 반환합니다.
	 *
	 * @param dto             마스킹 대상 일정 DTO
	 * @param readablePostIds viewer가 읽기 가능한 게시글 ID 집합
	 * @return 마스킹이 적용된 ScheduleDto
	 */
	public ScheduleDto maskIfUnreadable(ScheduleDto dto, Set<String> readablePostIds) {
		if (dto.targetPostId() == null || readablePostIds.contains(dto.targetPostId())) {
			return dto;
		}
		return ScheduleMapper.toWithoutTargetPost(dto);
	}

	/**
	 * 일정 DTO 목록에서 연결된 게시글 ID를 수집합니다.
	 */
	private Set<String> extractTargetPostIds(List<ScheduleDto> scheduleDtos) {
		return scheduleDtos.stream()
			.map(ScheduleDto::targetPostId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	/**
	 * 게시글 맵에서 삭제되지 않은 게시글의 게시판 ID 집합을 추출합니다.
	 */
	private Set<String> extractActiveBoardIds(Map<String, Post> postsById) {
		return postsById.values().stream()
			.filter(post -> !Boolean.TRUE.equals(post.getIsDeleted()))
			.map(Post::getBoard)
			.filter(Objects::nonNull)
			.map(Board::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	/**
	 * 게시글 목록에서 viewer가 읽을 수 있는 게시글 ID 집합을 반환합니다.
	 * 권한 없음(BaseRunTimeV2Exception)은 해당 게시글을 건너뜁니다.
	 */
	private Set<String> filterReadablePostIds(Collection<Post> posts, User viewer,
		Map<String, BoardConfig> boardConfigMap, Map<String, Set<String>> boardAdminMap) {
		Set<String> readablePostIds = new HashSet<>();
		for (Post post : posts) {
			if (Boolean.TRUE.equals(post.getIsDeleted()) || post.getBoard() == null
				|| post.getBoard().getId() == null) {
				continue;
			}

			String boardId = post.getBoard().getId();
			BoardConfig boardConfig = boardConfigMap.get(boardId);
			if (boardConfig == null) {
				continue;
			}

			try {
				Set<String> boardAdminIds = boardAdminMap.getOrDefault(boardId, Set.of());
				PostValidator.validateRead(viewer, boardConfig, boardAdminIds);
				readablePostIds.add(post.getId());
			} catch (BaseRunTimeV2Exception ignored) {
				// 권한 없음 → targetPostId 마스킹 대상
			}
		}
		return readablePostIds;
	}
}
