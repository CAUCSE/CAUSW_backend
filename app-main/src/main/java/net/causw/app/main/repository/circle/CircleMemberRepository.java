package net.causw.app.main.repository.circle;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.circle.CircleMember;
import net.causw.app.main.domain.model.enums.circle.CircleMemberStatus;

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
