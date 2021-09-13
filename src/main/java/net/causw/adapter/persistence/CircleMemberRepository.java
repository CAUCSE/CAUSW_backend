package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, String> {
    Optional<CircleMember> findByUser_IdAndCircle_Id(String userId, String circleId);
}
