package net.causw.app.main.domain.community.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.QBoardConfig;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardConfigQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	public List<BoardConfig> findByBoardIdsIn(List<String> boardIds) {

		QBoardConfig boardConfig = QBoardConfig.boardConfig;

		return jpaQueryFactory
			.selectFrom(boardConfig)
			.where(boardConfig.boardId.in(boardIds))
			.fetch();
	}

	public Optional<BoardConfig> findByBoardId(String boardId) {
		QBoardConfig boardConfig = QBoardConfig.boardConfig;

		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(boardConfig)
			.where(boardConfig.boardId.eq(boardId))
			.fetchOne());
	}

	public Integer findMaxDisplayOrder() {
		QBoardConfig boardConfig = QBoardConfig.boardConfig;
		Integer max = jpaQueryFactory
			.select(boardConfig.displayOrder.max())
			.from(boardConfig)
			.fetchOne();
		return max != null ? max : 0;
	}

	/**
	 * 사용자 상태에 따라 접근 가능한 게시판 ID 목록을 조회합니다.
	 * VISIBLE이고 사용자의 ReadScope에 맞는 게시판만 조회합니다.
	 *
	 * @param academicStatus 사용자 상태
	 * @return 게시판 ID 목록
	 */
	public List<String> findAccessibleBoardIdsByUserState(AcademicStatus academicStatus) {
		QBoardConfig boardConfig = QBoardConfig.boardConfig;

		// UserState에 따른 ReadScope 조건
		BooleanExpression readScopeCondition;
		if (academicStatus == AcademicStatus.GRADUATED) {
			// 졸업생: GRADUATED 또는 BOTH
			readScopeCondition = boardConfig.readScope.eq(BoardReadScope.GRADUATED)
				.or(boardConfig.readScope.eq(BoardReadScope.BOTH));
		} else if (academicStatus == AcademicStatus.ENROLLED || academicStatus == AcademicStatus.LEAVE_OF_ABSENCE) {
			// 재학생 (ENROLLED, AWAIT 등): ENROLLED 또는 BOTH
			readScopeCondition = boardConfig.readScope.eq(BoardReadScope.ENROLLED)
				.or(boardConfig.readScope.eq(BoardReadScope.BOTH));
		} else {
			readScopeCondition = boardConfig.readScope.eq(BoardReadScope.BOTH);
		}

		return jpaQueryFactory
			.select(boardConfig.boardId)
			.from(boardConfig)
			.where(boardConfig.visibility.eq(BoardVisibility.VISIBLE)
				.and(readScopeCondition))
			.fetch();
	}
}
