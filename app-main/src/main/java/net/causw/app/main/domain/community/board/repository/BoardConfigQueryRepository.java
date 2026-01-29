package net.causw.app.main.domain.community.board.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.QBoardConfig;

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
}
