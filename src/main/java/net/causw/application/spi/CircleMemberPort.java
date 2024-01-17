package net.causw.application.spi;

import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.user.UserDomainModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CircleMemberPort {
    Optional<CircleMemberDomainModel> findById(String id);

    List<CircleMemberDomainModel> findByUserId(String userId);

    Map<String, CircleMemberDomainModel> findCircleByUserId(String userId);

    List<CircleDomainModel> getCircleListByUserId(String userId);

    List<CircleMemberDomainModel> findByCircleId(String circleId, CircleMemberStatus status);

    Optional<CircleMemberDomainModel> findByUserIdAndCircleId(String userId, String circleId);

    Long getNumMember(String id);

    CircleMemberDomainModel create(UserDomainModel userDomainModel, CircleDomainModel circleDomainModel);

    Optional<CircleMemberDomainModel> updateStatus(String applicationId, CircleMemberStatus targetStatus);
}
