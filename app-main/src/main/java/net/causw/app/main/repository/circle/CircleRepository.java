package net.causw.app.main.repository.circle;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.circle.Circle;

@Repository
public interface CircleRepository extends JpaRepository<Circle, String> {
	List<Circle> findByLeader_Id(String leader_id);

	Optional<Circle> findByName(String name);

	Optional<Circle> findByIdAndIsDeletedIsFalse(String id);

	List<Circle> findAllByIsDeletedIsFalse();
}
