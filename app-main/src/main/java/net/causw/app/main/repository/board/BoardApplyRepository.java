package net.causw.app.main.repository.board;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.board.BoardApply;
import net.causw.app.main.domain.model.enums.board.BoardApplyStatus;

@Repository
public interface BoardApplyRepository extends JpaRepository<BoardApply, String> {
	List<BoardApply> findAllByAcceptStatus(BoardApplyStatus boardApplyStatus);

	Optional<BoardApply> findById(String id);
}
