package net.causw.app.main.domain.community.board.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.entity.QBoard;
import net.causw.app.main.domain.community.board.entity.QBoardAdmin;
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
	 * 사용자에게 쓰기 권한이 있는 게시판 ID 목록을 조회합니다.(VISIBLE 체크 포함)
	 * ONLY_ADMIN 쓰기 범위를 체크할 때 board 하나 당 하나의 쿼리가 나가는 N+1 방지를 위해 join으로 처리했습니다.
	 * board와 boardConfig를 join하여 board의 visibility와 writeScope를 가져옵니다.
	 * writeScope가 {@code ALL_USER}일 경우는 visible할 경우 포함시키고,
	 * writeScope가 {@code ONLY_ADMIN}일 경우는 board와 boardAdmin의 left join을 통해 해당 board의 admin에 userId가 포함되는지 체크합니다.
	 *
	 * @param userId 대상이 되는 user의 Id
	 * @return 게시판 ID 목록
	 */
	public List<String> findWritableBoardsByUserId(String userId) {
		QBoard board = QBoard.board;
		QBoardConfig boardConfig = QBoardConfig.boardConfig;
		QBoardAdmin boardAdmin = QBoardAdmin.boardAdmin;

		return jpaQueryFactory
			.select(board.id)
			.from(board)
			// 게시판 설정 정보와 Inner Join
			.join(boardConfig).on(board.id.eq(boardConfig.boardId))
			// 관리자 테이블과 Left Join
			.leftJoin(boardAdmin).on(
				board.id.eq(boardAdmin.boardId)
					.and(boardAdmin.userId.eq(userId)))
			// 필터링 조건
			.where(
				visible(boardConfig),
				boardConfig.writeScope.eq(BoardWriteScope.ALL_USER)
					.or(
						boardConfig.writeScope.eq(BoardWriteScope.ONLY_ADMIN)
							.and(boardAdmin.userId.isNotNull())))
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
