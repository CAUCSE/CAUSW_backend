package net.causw.app.main.domain.asset.locker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.locker.entity.LockerLocation;

@Repository
public interface LockerLocationRepository extends JpaRepository<LockerLocation, String> {
	Optional<LockerLocation> findByName(String name);

	@Query("select ll from LockerLocation ll order by ll.createdAt")
	List<LockerLocation> findAllOrderByCreatedAt();
}
