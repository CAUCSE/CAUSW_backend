package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.locker.LockerLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LockerLocationRepository extends JpaRepository<LockerLocation, String> {
    Optional<LockerLocation> findByName(String name);
}
