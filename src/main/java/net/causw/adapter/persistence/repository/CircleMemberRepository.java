package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.circle.CircleMember;
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

    @Query(value = "SELECT COUNT(*) " +
            "FROM TB_CIRCLE_MEMBER " +
            "WHERE TB_CIRCLE_MEMBER.circle_id = :id AND TB_CIRCLE_MEMBER.status = 'MEMBER'", nativeQuery = true)
    long getNumMember(@Param("id") String id);
}
