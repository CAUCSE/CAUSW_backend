package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordFormatValidator extends AbstractValidator {

    private final String password;

    private PasswordFormatValidator(String password) {
        this.password = password;
    }

    public static PasswordFormatValidator of(String password) {
        return new PasswordFormatValidator(password);
    }

    @Override
    public void validate() {
        if (!this.validatePassword()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_USER_DATA_REQUEST,
                    "비밀번호 형식이 잘못되었습니다."
            );
        }
    }

    public boolean validatePassword() {
        String passwordPolicy = "((?=.*[a-z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,})";

        Pattern pattern_password = Pattern.compile(passwordPolicy);
        Matcher matcher_password = pattern_password.matcher(this.password);

        return matcher_password.matches();
    }
}
