package net.causw.app.main.domain.community.board.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardAdmin;
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

        /**
         * 여러 게시판 ID에 대해 boardId -> 관리자 userId 목록 Map을 반환합니다.
         *
         * @param boardIds 게시판 ID 컬렉션
         * @return boardId를 키로 하는 관리자 userId 목록 Map
         */
        public Map<String, List<String>> findAdminIdsByBoardIds(Collection<String> boardIds) {
                if (boardIds == null || boardIds.isEmpty()) {
                        return Map.of();
                }
                QBoardAdmin boardAdmin = QBoardAdmin.boardAdmin;

                List<BoardAdmin> results = jpaQueryFactory.selectFrom(boardAdmin)
                        .where(boardAdmin.boardId.in(boardIds))
                        .fetch();

                return results.stream()
                        .collect(Collectors.groupingBy(
                                BoardAdmin::getBoardId,
                                Collectors.mapping(BoardAdmin::getUserId, Collectors.toList())));
        }
}
