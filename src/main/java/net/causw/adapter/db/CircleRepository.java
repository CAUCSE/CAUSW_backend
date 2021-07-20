package net.causw.adapter.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircleRepository extends JpaRepository<Circle, String> {
}
