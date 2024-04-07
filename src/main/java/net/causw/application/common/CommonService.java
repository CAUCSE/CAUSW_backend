package net.causw.application.common;

import lombok.RequiredArgsConstructor;
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonService {
    private final FlagPort flagPort;
    private final UserPort userPort;

    @Transactional
    public Boolean createFlag(
            String loginUserId,
            String key,
            Boolean value
    ) {
        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(userDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.flagPort.findByKey(key).ifPresent(
                flag -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.FLAG_ALREADY_EXIST
                    );
                }
        );

        return this.flagPort.create(key, value);
    }

    @Transactional
    public Boolean updateFlag(
            String loginUserId,
            String key,
            Boolean value
    ) {
        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(userDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return this.flagPort.update(key, value).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.FLAG_UPDATE_FAILED
                )
        );
    }
}
