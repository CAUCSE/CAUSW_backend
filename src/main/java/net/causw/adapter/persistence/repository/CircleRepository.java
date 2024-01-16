package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.circle.Circle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircleRepository extends JpaRepository<Circle, String> {
    @Query(value = "SELECT * from TB_CIRCLE where TB_CIRCLE.leader_id = ?1", nativeQuery = true)
    Optional<Circle> findByLeaderId(String leader_id);

    Optional<Circle> findByName(String name);

    List<Circle> findAllByIsDeletedIsFalse();
}
