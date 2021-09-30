package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class TargetIsNullValidator extends AbstractValidator {

    private final String studentId;

    private TargetIsNullValidator(String studentId) {
        this.studentId = studentId;
    }

    public static TargetIsNullValidator of(String studentId) {
        return new TargetIsNullValidator(studentId);
    }

    @Override
    public void validate() {
        if (this.studentId == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_STUDENT_ID,
                    "Student Id of this user is null"
            );
        }
    }
}
