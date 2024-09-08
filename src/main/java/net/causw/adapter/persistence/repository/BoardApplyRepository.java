package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.board.BoardApply;
import net.causw.domain.model.enums.BoardApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardApplyRepository extends JpaRepository<BoardApply, String> {
    List<BoardApply> findAllByAcceptStatus(BoardApplyStatus boardApplyStatus);

    BoardApply findByBoardName(String boardName);
}
