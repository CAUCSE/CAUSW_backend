package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.application.spi.TextFieldPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.locker.LockerDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.ExtendLockerExpiredAtValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionExtend implements LockerAction {
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel updaterDomainModel,
            LockerPort lockerPort,
            LockerLogPort lockerLogPort,
            FlagPort flagPort,
            TextFieldPort textFieldPort
    ) {
        if (lockerDomainModel.getUser().isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사용 중인 사물함이 아닙니다."
            );
        }

        if (!updaterDomainModel.getId().equals(lockerDomainModel.getUser().get().getId()))
            ValidatorBucket.of()
                    .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                    .validate();

        LocalDateTime expiredAtToExtend = LocalDateTime.parse(textFieldPort.findByKey(StaticValue.EXPIRED_AT).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "사물함 반납 기한을 설정하지 않았습니다."
                )
        ), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Optional.ofNullable(lockerDomainModel.getExpiredAt()).ifPresent(expiredAt ->
                ValidatorBucket.of()
                        .consistOf(ExtendLockerExpiredAtValidator.of(
                                expiredAt,
                                expiredAtToExtend))
                        .validate());


        lockerDomainModel.extendExpireDate(expiredAtToExtend);

        return lockerPort.update(
                lockerDomainModel.getId(),
                lockerDomainModel
        );
    }
}
