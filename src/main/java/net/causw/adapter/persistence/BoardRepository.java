package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findByCircle_IdAndIsDeletedIsFalse(String circleId);

    List<Board> findByCircle_IdIsNullAndIsDeletedIsFalse();

    List<Board> findTop3ByCircle_IdIsNullAndIsDeletedIsFalseOrderByCreatedAtAsc();
}
