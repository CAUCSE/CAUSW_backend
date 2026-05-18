package net.causw.app.main.domain.community.board.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.entity.QBoard;
import net.causw.app.main.domain.community.board.entity.QBoardAdmin;
import net.causw.app.main.domain.community.board.entity.QBoardConfig;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
	 * 사용자에게 쓰기 권한이 있는 게시판 목록을 조회합니다.(VISIBLE, isDeleted 체크 포함, 표시 순서에 따라 정렬 포함)
	 * ONLY_ADMIN 쓰기 범위를 체크할 때 board 하나 당 하나의 쿼리가 나가는 N+1 방지를 위해
	 * WriteScope가 {@code ONLY_ADMIN}일 경우에만 exists()를 통해 쓰기 가능 여부를 체크하도록 하였습니다.
	 * board와 boardConfig를 join하여 board의 visibility와 IsDeleted, writeScope를 가져옵니다.
	 * IsDeleted가 false이고, visable 할 때,
	 * writeScope가 {@code ALL_USER}일 경우 리스트에 포함시키고,
	 * writeScope가 {@code ONLY_ADMIN}일 경우는 boardAdmin에 해당 boardId, userId가 exist하는지 체크합니다.
	 *
	 * @param userId 대상이 되는 user의 Id
	 * @param isAdmin 대상이 되는 user가 ADMIN 권한을가지고 있는지 여부
	 * @return 쓰기 가능한 게시판 목록
	 */
	public List<Board> findWritableBoardsByUserId(String userId, boolean isAdmin) {
		QBoard board = QBoard.board;
		QBoardConfig boardConfig = QBoardConfig.boardConfig;
		QBoardAdmin boardAdmin = QBoardAdmin.boardAdmin;

		return jpaQueryFactory
			.selectFrom(board)
			// 게시판 설정 정보와 Inner Join
			.join(boardConfig).on(board.id.eq(boardConfig.boardId))
			// 필터링 조건(isDeleted, visibility, writeScope 체크)
			.where(
				board.isDeleted.isFalse(),
				visible(boardConfig),
				// ALL_USER 이거나 ONLY_ADMIN이고 boardAdmin에 boardId와 userId 컬럼이 존재할 경우
				// isAdmin인 경우는 writeScope 체크 없이 모든 게시판 포함
				isAdmin ? null : boardConfig.writeScope.eq(BoardWriteScope.ALL_USER)
					.or(
						boardConfig.writeScope.eq(BoardWriteScope.ONLY_ADMIN)
							.and(
								JPAExpressions.selectOne()
									.from(boardAdmin)
									.where(
										boardAdmin.boardId.eq(board.id),
										boardAdmin.userId.eq(userId))
									.exists())))
			.orderBy(boardConfig.displayOrder.asc())
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
