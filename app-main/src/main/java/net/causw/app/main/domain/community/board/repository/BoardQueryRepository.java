package net.causw.app.main.domain.community.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.QBoard;
import net.causw.app.main.domain.community.board.entity.QBoardConfig;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 모든 게시판을 표시 순서에 따라 조회합니다.
	 * 게시판 설정이 있는 경우에만 조회합니다.
	 * @return
	 */
	public List<Board> findWithConditionOrderByDisplayOrder(BoardQueryCondition boardQueryCondition) {

		QBoard board = QBoard.board;
		QBoardConfig boardConfig = QBoardConfig.boardConfig;

		return jpaQueryFactory
			.selectFrom(board)
			.join(boardConfig).on(board.id.eq(boardConfig.boardId))
			.where(
				notDeleted(),
				boardQueryCondition.keyword() != null
					? board.name.containsIgnoreCase(boardQueryCondition.keyword())
					: null,
				boardQueryCondition.isAnonymous() != null
					? boardConfig.isAnonymous.eq(boardQueryCondition.isAnonymous())
					: null,
				boardQueryCondition.readScope() != null
					? boardConfig.readScope.eq(boardQueryCondition.readScope())
					: null,
				boardQueryCondition.writeScope() != null
					? boardConfig.writeScope.eq(boardQueryCondition.writeScope())
					: null,
				boardQueryCondition.isNotice() != null
					? boardConfig.isNotice.eq(boardQueryCondition.isNotice())
					: null)
			.orderBy(boardConfig.displayOrder.asc())
			.fetch();
	}

	/**
	 * 게시판 아이디로 게시판을 조회합니다.
	 * @param boardId 게시판 아이디
	 * @return 게시판 엔티티 (optional)
	 */
	public Optional<Board> findById(String boardId) {

		QBoard board = QBoard.board;

		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(board)
			.where(board.id.eq(boardId))
			.where(notDeleted())
			.fetchOne());
	}

	/**
	 * 게시판 ID 목록으로 게시판을 조회합니다. 삭제되지 않은 게시판만 조회하며, 표시 순서(displayOrder) 오름차순으로 반환합니다.
	 * @param boardIds 게시판 ID 목록
	 * @return 삭제되지 않은 게시판 엔티티 목록 (displayOrder 오름차순)
	 */
	public List<Board> findAllByIdsNotDeleted(List<String> boardIds) {
		if (boardIds == null || boardIds.isEmpty()) {
			return List.of();
		}
		QBoard board = QBoard.board;
		QBoardConfig boardConfig = QBoardConfig.boardConfig;

		return jpaQueryFactory
			.selectFrom(board)
			.join(boardConfig).on(board.id.eq(boardConfig.boardId))
			.where(board.id.in(boardIds))
			.where(notDeleted())
			.orderBy(boardConfig.displayOrder.asc())
			.fetch();
	}

	public BooleanExpression notDeleted() {
		QBoard board = QBoard.board;
		return board.isDeleted.eq(false);
	}
}
