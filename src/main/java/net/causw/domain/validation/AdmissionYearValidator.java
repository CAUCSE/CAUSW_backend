package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.StaticValue;

import java.util.Calendar;

public class AdmissionYearValidator {

    public void isValid(Integer admissionYear) {
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println(admissionYear);

        if (!validateAdmissionYear(admissionYear)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_USER_DATA_REQUEST,
                    "입학년도를 다시 확인해주세요."
            );
        }
    }

    public boolean validateAdmissionYear(Integer admissionYear) {
        if (admissionYear < StaticValue.CAUSW_CREATED) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        int presentYear = cal.get(Calendar.YEAR);
        return admissionYear <= presentYear;
    }
}
