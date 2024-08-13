package net.causw.domain.validation;

import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordCorrectValidator {

    private final PasswordEncoder passwordEncoder;

    @Setter
    private String srcPassword;

    public void validate(String srcPassword, String dstPassword) {
        if (!passwordEncoder.matches(dstPassword, srcPassword)) {
            throw new UnauthorizedException(
                    ErrorCode.INVALID_SIGNIN,
                    "비밀번호를 잘못 입력했습니다."
            );
        }
    }
}
