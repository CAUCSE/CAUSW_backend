package net.causw.app.main.domain.community.form.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.form.entity.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
	Optional<Form> findByIdAndIsDeleted(String id, Boolean isDeleted);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
		UPDATE Form f
		SET f.isDeleted = true
		WHERE f.isDeleted = false
		AND f.id IN (
			SELECT p.form.id
			FROM Post p
			WHERE p.board.id = :boardId
			AND p.form IS NOT NULL
		)
		""")
	int softDeleteAllByBoardId(@Param("boardId") String boardId);
}
