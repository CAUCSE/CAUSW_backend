package net.causw.application.spi;

import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.UserDomainModel;

import java.util.List;
import java.util.Optional;

public interface CircleMemberPort {
    Optional<CircleMemberDomainModel> findById(String id);

    List<CircleMemberDomainModel> findByUserId(String userId);

    List<CircleDomainModel> getCircleListByUserId(String userId);

    List<CircleMemberDomainModel> findByCircleId(String circleId, CircleMemberStatus status);

    Optional<CircleMemberDomainModel> findByUserIdAndCircleId(String userId, String circleId);

    CircleMemberDomainModel create(UserDomainModel userDomainModel, CircleDomainModel circleDomainModel);

    Optional<CircleMemberDomainModel> updateStatus(String applicationId, CircleMemberStatus targetStatus);
}
