package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PasswordFormatValidator {

    public void validate(String password) {
        if (!this.validatePassword(password)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_USER_DATA_REQUEST,
                    "비밀번호 형식이 잘못되었습니다."
            );
        }
    }

    public boolean validatePassword(String password) {
        String passwordPolicy = "((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,})";

        Pattern pattern_password = Pattern.compile(passwordPolicy);
        Matcher matcher_password = pattern_password.matcher(password);

        return matcher_password.matches();
    }
}
