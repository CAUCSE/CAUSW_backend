package net.causw.application;

import net.causw.application.spi.CircleMemberPort;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;

public class CircleMemberAuthentication {
    public static void authenticate(
            CircleMemberPort circleMemberPort,
            UserDomainModel user,
            Optional<CircleDomainModel> circle
    ) {
        circle
                .filter(circleDomainModel -> !user.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = circleMemberPort.findByUserIdAndCircleId(
                                    user.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            ValidatorBucket.of()
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ))
                                    .validate();
                        }
                );
    }

    public static void authenticateLeader(
            CircleMemberPort circleMemberPort,
            UserDomainModel user,
            Optional<CircleDomainModel> circle
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        circle
                .filter(circleDomainModel -> !user.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = circleMemberPort.findByUserIdAndCircleId(
                                    user.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (user.getRole().equals(Role.LEADER_CIRCLE)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new InternalServerException(
                                                                ErrorCode.INTERNAL_SERVER,
                                                                "The board has circle without circle leader"
                                                        )
                                                ),
                                                user.getId()
                                        ));
                            }

                            validatorBucket.validate();
                        }
                );
    }
}
