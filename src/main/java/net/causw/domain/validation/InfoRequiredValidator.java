package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserDomainModel;

public class InfoRequiredValidator extends AbstractValidator{
    private final UserDomainModel userDomainModel;

    private InfoRequiredValidator(UserDomainModel userDomainModel) {
        this.userDomainModel = userDomainModel;
    }

    public static InfoRequiredValidator of(UserDomainModel userDomainModel) {
        return new InfoRequiredValidator(userDomainModel);
    }

    @Override
    public void validate() {

        if(userDomainModel.getAdmissionYear() == 0
                && userDomainModel.getName() == "temporary"
                && userDomainModel.getPassword() == "temporary"
                && userDomainModel.getStudentId() == null)
        {
            throw new UnauthorizedException(
                    ErrorCode.NEED_INFO,
                    "정보 기입이 필요합니다."
            );
        }
    }
}
