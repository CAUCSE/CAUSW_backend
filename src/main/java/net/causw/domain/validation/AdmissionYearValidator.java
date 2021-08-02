package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Calendar;

public class AdmissionYearValidator extends AbstractValidator {

    private final Integer admissionYear;

    private AdmissionYearValidator(Integer admissionYear) {
        this.admissionYear = admissionYear;
    }

    public static AdmissionYearValidator of(Integer admissionYear) {
        return new AdmissionYearValidator(admissionYear);
    }

    @Override
    public void validate() {
        if (!this.validateAdmissionYear()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_SIGNUP,
                    "Invalid sign up data: admission year"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }

    public boolean validateAdmissionYear() {
        if (this.admissionYear < 1972) { return false; }

        Calendar cal = Calendar.getInstance();
        int presentYear = cal.get(Calendar.YEAR);
        return this.admissionYear <= presentYear;
    }
}
