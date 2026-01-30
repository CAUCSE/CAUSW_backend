package net.causw.app.main.domain.community.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardAdmin;

@Repository
public interface BoardAdminRepository extends JpaRepository<BoardAdmin, Long> {

	void deleteByBoardId(String boardId);
}
