package net.causw.app.main.domain.community.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
	List<Board> findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(String circleId);

	List<Board> findByCircle_IdInAndIsDeletedFalseOrderByCreatedAtAsc(List<String> circleIdList);

	List<Board> findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(boolean isDeleted);

	List<Board> findByOrderByCreatedAtAsc();

	@Query(value = "SELECT * FROM tb_board WHERE tb_board.category = 'APP_NOTICE'", nativeQuery = true)
	Optional<Board> findAppNotice();

	Boolean existsByName(String name);

	Boolean existsByNameAndIdNot(String name, String id);

	Optional<Board> findByName(String name);

	List<Board> findByIsAlumniTrueAndIsDeletedFalseOrderByCreatedAtAsc();

	List<Board> findByIsHomeTrueAndIsAlumniTrueAndIsDeletedFalse();

	List<Board> findAllByCategory(String boardCategory);
}
