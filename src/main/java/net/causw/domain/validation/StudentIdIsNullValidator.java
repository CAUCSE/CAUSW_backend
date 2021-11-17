package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class StudentIdIsNullValidator extends AbstractValidator {

    private final String studentId;

    private StudentIdIsNullValidator(String studentId) {
        this.studentId = studentId;
    }

    public static StudentIdIsNullValidator of(String studentId) {
        return new StudentIdIsNullValidator(studentId);
    }

    @Override
    public void validate() {
        if (this.studentId == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_STUDENT_ID,
                    "학번이 입력되지 않았습니다."
            );
        }
    }
}
