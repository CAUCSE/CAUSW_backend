package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(String circleId);

    List<Board> findByCircle_IdOrderByCreatedAtAsc(String circleId);
    List<Board> findByOrderByCreatedAtAsc();
    List<Board> findByCircle_IdIsNullAndIsDeletedIsFalseOrderByCreatedAtAsc();
    List<Board> findByIsDeletedOrderByCreatedAtAsc(boolean IsDeleted);

    @Query(value = "SELECT * FROM TB_BOARD " +
            "WHERE TB_BOARD.name = '앱 공지사항' " +
            "OR TB_BOARD.name = '학생회 공지게시판' " +
            "OR TB_BOARD.name = '전체 자유게시판'", nativeQuery = true)
    List<Board> findBasicBoards();

    @Query(value = "SELECT * FROM TB_BOARD WHERE TB_BOARD.category = 'APP_NOTICE'", nativeQuery = true)
    Optional<Board> findAppNotice();
}
