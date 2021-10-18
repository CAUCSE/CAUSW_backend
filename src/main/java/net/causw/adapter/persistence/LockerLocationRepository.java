package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LockerLocationRepository extends JpaRepository<LockerLocation, String> {
}
