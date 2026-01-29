package net.causw.app.main.domain.community.board.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.QBoard;
import net.causw.app.main.domain.community.board.entity.QBoardConfig;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;

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
					: null
			)
			.orderBy(boardConfig.displayOrder.asc())
			.fetch();
	}
}
