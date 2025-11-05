package net.causw.app.main.domain.moving.repository.locker;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.moving.model.entity.locker.LockerLocation;

@Repository
public interface LockerLocationRepository extends JpaRepository<LockerLocation, String> {
	Optional<LockerLocation> findByName(String name);
}
