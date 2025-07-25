package net.causw.app.main.domain.validation;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordCorrectValidator extends AbstractValidator {

    private final PasswordEncoder passwordEncoder;

    private final String srcPassword;
    private final String dstPassword;

    private PasswordCorrectValidator(PasswordEncoder passwordEncoder, String srcPassword, String dstPassword) {
        this.passwordEncoder = passwordEncoder;
        this.srcPassword = srcPassword;
        this.dstPassword = dstPassword;
    }

    public static PasswordCorrectValidator of(PasswordEncoder passwordEncoder, String srcPassword, String dstPassword) {
        return new PasswordCorrectValidator(passwordEncoder, srcPassword, dstPassword);
    }

    @Override
    public void validate() {
        if (!passwordEncoder.matches(dstPassword, srcPassword)) {
            throw new UnauthorizedException(
                    ErrorCode.INVALID_SIGNIN,
                    "비밀번호를 잘못 입력했습니다."
            );
        }
    }
}
