package net.causw.app.main.domain.community.board.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.QBoardConfig;

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
	 * 사용자 학적 상태에 따라 접근 가능한 게시판 ID 목록을 조회합니다.
	 * VISIBLE이고 사용자의 ReadScope에 맞는 게시판만 조회합니다.
	 *
	 * @param boardReadScopes 조회할 boardReadScope 집합
	 * @return 게시판 ID 목록
	 */
	public List<String> findBoardsByReadScopes(Set<BoardReadScope> boardReadScopes) {
		QBoardConfig boardConfig = QBoardConfig.boardConfig;

		return jpaQueryFactory
			.select(boardConfig.boardId)
			.from(boardConfig)
			.where(visible(boardConfig)
				.and(boardConfig.readScope.in(boardReadScopes)))
			.fetch();
	}

	/**
	 * 공지사항 게시판(boardConfig.isNotice=true) 중에서 사용자의 ReadScope에 맞는 게시판 설정을 조회합니다.
	 * @param readScopes 조회할 boardReadScope 집합
	 * @return 공지사항 게시판 설정 목록
	 *
	 */
	public List<BoardConfig> findAllByIsNoticeTrueAndReadScopeIn(Set<BoardReadScope> readScopes) {
		QBoardConfig boardConfig = QBoardConfig.boardConfig;
		return jpaQueryFactory
			.selectFrom(boardConfig)
			.where(boardConfig.isNotice.eq(true)
				.and(boardConfig.readScope.in(readScopes))
				.and(visible(boardConfig)))
			.fetch();
	}

	private static BooleanExpression visible(QBoardConfig boardConfig) {
		return boardConfig.visibility.eq(BoardVisibility.VISIBLE);
	}
}
