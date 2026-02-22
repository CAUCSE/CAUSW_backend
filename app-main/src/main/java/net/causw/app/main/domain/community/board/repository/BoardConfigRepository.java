package net.causw.app.main.domain.community.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.BoardConfig;

@Repository
public interface BoardConfigRepository extends JpaRepository<BoardConfig, Long> {

	List<BoardConfig> findAllByIsNoticeTrue();

	boolean existsByBoardIdAndIsNoticeTrue(String boardId);
}
