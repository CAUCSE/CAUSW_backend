package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(String circleId);

    List<Board> findByCircle_IdIsNullAndIsDeletedIsFalseOrderByCreatedAtAsc();

    List<Board> findTop3ByCircle_IdIsNullAndIsDeletedIsFalseOrderByCreatedAtAsc();

    @Query(value = "SELECT * FROM TB_BOARD where TB_BOARD.category = 'APP_NOTICE'", nativeQuery = true)
    Optional<Board> findAppNotice();
}
