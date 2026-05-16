package net.causw.app.main.domain.campus.schedule.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleWriter;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.util.PostValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
	private final ScheduleWriter scheduleWriter;
	private final ScheduleReader scheduleReader;
	private final PostReader postReader;
	private final BoardConfigReader boardConfigReader;

	@Transactional
	public ScheduleDto save(ScheduleDto dto) {
		return ScheduleMapper.to(scheduleWriter.create(dto));
	}

	@Transactional
	public ScheduleDto update(String scheduleId, ScheduleDto dto) {
		Schedule schedule = scheduleReader.findById(scheduleId);
		return ScheduleMapper.to(scheduleWriter.update(schedule, dto));
	}

	@Transactional
	public void delete(String scheduleId) {
		scheduleWriter.deleteById(scheduleId);
	}

	@Transactional(readOnly = true)
	public List<ScheduleDto> findByCondition(LocalDateTime from, LocalDateTime to, Collection<ScheduleType> types) {
		return scheduleReader.findByCondition(from, to, types).stream().map(ScheduleMapper::to)
			.toList();
	}

	@Transactional(readOnly = true)
	public ScheduleDto findById(String scheduleId) {
		return ScheduleMapper.to(scheduleReader.findById(scheduleId));
	}

	/**
	 * 조건에 따라 일정을 조회하고, 연결된 게시물(targetPostId)에 대한 읽기 권한이 없는 경우 null로 마스킹합니다.
	 *
	 * @param from   시작 일시
	 * @param to     종료 일시
	 * @param types  일정 유형 필터
	 * @param viewer 현재 요청 사용자 (null이면 모두 마스킹)
	 * @return targetPostId가 권한에 따라 마스킹된 일정 목록
	 */
	@Transactional(readOnly = true)
	public List<ScheduleDto> findByConditionWithMasking(LocalDateTime from, LocalDateTime to,
		Collection<ScheduleType> types, User viewer) {
		return scheduleReader.findByCondition(from, to, types).stream()
			.map(ScheduleMapper::to)
			.map(dto -> maskTargetPostIdIfNeeded(dto, viewer))
			.toList();
	}

	/**
	 * 특정 ID의 일정을 조회하고, 연결된 게시물(targetPostId)에 대한 읽기 권한이 없는 경우 null로 마스킹합니다.
	 *
	 * @param scheduleId 일정 ID
	 * @param viewer     현재 요청 사용자 (null이면 마스킹)
	 * @return targetPostId가 권한에 따라 마스킹된 일정
	 */
	@Transactional(readOnly = true)
	public ScheduleDto findByIdWithMasking(String scheduleId, User viewer) {
		return maskTargetPostIdIfNeeded(ScheduleMapper.to(scheduleReader.findById(scheduleId)), viewer);
	}

	/**
	 * 사용자가 연결된 게시물을 읽을 수 없는 경우 targetPostId를 null로 마스킹합니다.
	 */
	private ScheduleDto maskTargetPostIdIfNeeded(ScheduleDto dto, User viewer) {
		if (dto.targetPostId() == null) {
			return dto;
		}
		if (!canViewerReadPost(viewer, dto.targetPostId())) {
			return ScheduleDto.builder()
				.id(dto.id())
				.title(dto.title())
				.type(dto.type())
				.start(dto.start())
				.end(dto.end())
				.creator(dto.creator())
				.targetPostId(null)
				.build();
		}
		return dto;
	}

	/**
	 * 사용자가 특정 게시글을 읽을 수 있는지 확인합니다.
	 * post·board 도메인의 implementation 계층(PostReader, BoardConfigReader)과
	 * PostValidator 유틸리티를 직접 사용하여 권한을 검증합니다.
	 *
	 * @param viewer 조회 요청 사용자 (null이면 false 반환)
	 * @param postId 게시글 ID
	 * @return 읽기 가능 여부
	 */
	private boolean canViewerReadPost(User viewer, String postId) {
		if (viewer == null || postId == null) {
			return false;
		}
		try {
			Post post = postReader.findById(postId);
			if (post.getIsDeleted()) {
				return false;
			}
			BoardConfig boardConfig = boardConfigReader.getByBoardId(post.getBoard().getId());
			List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
			PostValidator.validateRead(viewer, boardConfig, boardAdminIds);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

