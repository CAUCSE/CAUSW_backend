package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.ExtendLockerExpiredAtValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
public class LockerActionExtend implements LockerAction {
    @Override
    public Optional<Locker> updateLockerDomainModel(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ) {
        if (locker.getUser().isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.LOCKER_UNUSED
            );
        }

        if (!user.getId().equals(locker.getUser().get().getId()))
            ValidatorBucket.of()
                    .consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
                    .validate();

        LocalDateTime expiredAtToExtend = LocalDateTime.parse(commonService.findByKeyInTextField(StaticValue.EXPIRED_AT).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.LOCKER_RETURN_TIME_NOT_SET
                )
        ), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        Optional.ofNullable(locker.getExpireDate()).ifPresent(expiredAt ->
                ValidatorBucket.of()
                        .consistOf(ExtendLockerExpiredAtValidator.of(
                                expiredAt,
                                expiredAtToExtend))
                        .validate());


        locker.extendExpireDate(expiredAtToExtend);

        return Optional.of(locker);
    }
}
