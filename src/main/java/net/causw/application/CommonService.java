package net.causw.application;

import net.causw.application.spi.FlagPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class CommonService {
    private final FlagPort flagPort;
    private final UserPort userPort;

    public CommonService(
            FlagPort flagPort,
            UserPort userPort
    ) {
        this.flagPort = flagPort;
        this.userPort = userPort;
    }

    @Transactional
    public Boolean createFlag(
            String userId,
            String key,
            Boolean value
    ) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(userDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.flagPort.findByKey(key).ifPresent(
                flag -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            "이미 존재하는 플래그 입니다."
                    );
                }
        );

        return this.flagPort.create(key, value);
    }

    @Transactional
    public Boolean updateFlag(
            String userId,
            String key,
            Boolean value
    ) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(userDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return this.flagPort.update(key, value).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "플래그를 업데이트 하지 못했습니다."
                )
        );
    }
}
