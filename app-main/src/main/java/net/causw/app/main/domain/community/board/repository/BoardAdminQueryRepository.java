package net.causw.app.main.domain.community.board.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.QBoardAdmin;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardAdminQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	public List<String> findAdminIdsByBoardId(String boardId) {
		QBoardAdmin boardAdmin = QBoardAdmin.boardAdmin;

		return jpaQueryFactory.selectFrom(boardAdmin)
			.where(boardAdmin.boardId.eq(boardId))
			.select(boardAdmin.userId)
			.fetch();
	}
}
