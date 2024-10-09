package net.causw.adapter.persistence.repository.circle;

import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.domain.model.enums.circle.CircleMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, String> {
    Optional<CircleMember> findByUser_IdAndCircle_Id(String userId, String circleId);

    List<CircleMember> findByUser_Id(String userId);

    List<CircleMember> findByCircle_Id(String circleId);

    List<CircleMember> findByCircle_IdAndStatus(String circleId, CircleMemberStatus status);

    @Query("SELECT COUNT(cm) " +
            "FROM CircleMember cm " +
            "WHERE cm.circle.id = :id AND cm.status = 'MEMBER'")
    long getNumMember(@Param("id") String id);
}
