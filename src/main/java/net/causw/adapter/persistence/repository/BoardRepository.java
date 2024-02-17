package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(String circleId);

    @Query(value = "SELECT * FROM tb_board AS b WHERE b.circle_id IN :circleIdList ORDER BY b.created_at ASC", nativeQuery = true)
    List<Board> findByCircle_IdOrderByCreatedAtAsc(@Param("circleIdList") List<String> circleIdList);

    @Query(value = "SELECT * FROM tb_board AS b WHERE (b.circle_id NOT IN :circleIdList OR b.circle_id IS NULL) AND b.is_deleted = false ORDER BY b.created_at ASC", nativeQuery = true)
    List<Board> findByCircle_IdNotInAndIsDeletedIsFalseOrderByCreatedAtAsc(@Param("circleIdList") List<String> circleIdList);

    List<Board> findByOrderByCreatedAtAsc();
    List<Board> findByIsDeletedOrderByCreatedAtAsc(boolean IsDeleted);

    @Query(value = "SELECT * FROM tb_board " +
            "WHERE TB_BOARD.name = '앱 공지사항' " +
            "OR TB_BOARD.name = '학생회 공지게시판' " +
            "OR TB_BOARD.name = '전체 자유게시판'", nativeQuery = true)
    List<Board> findBasicBoards();

    @Query(value = "SELECT * FROM tb_board WHERE tb_board.category = 'APP_NOTICE'", nativeQuery = true)
    Optional<Board> findAppNotice();
}
