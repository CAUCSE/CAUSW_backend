package net.causw.application.common;

import lombok.RequiredArgsConstructor;
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
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
            String loginUserId,
            String key,
            Boolean value
    ) {
        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
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
