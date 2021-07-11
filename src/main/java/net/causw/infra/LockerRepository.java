package net.causw.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LockerRepository extends JpaRepository<Locker, String> {
    Optional<Locker> findByLockerNumber(Long lockerNumber);
}
