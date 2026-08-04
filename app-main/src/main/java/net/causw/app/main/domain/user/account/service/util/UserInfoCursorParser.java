package net.causw.app.main.domain.user.account.service.util;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.user.account.service.dto.UserInfoCursor;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class UserInfoCursorParser {

    private final JsonMapper jsonMapper;

    public UserInfoCursor decode(String cursor) {
        try {
            byte[] json = Base64.getDecoder()
                    .decode(cursor);

            UserInfoCursor decodedCursor = jsonMapper.readValue(json, UserInfoCursor.class);

            validate(decodedCursor);
        } catch (Exception e) {
            throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
        }
    }

    public String encode(UserInfoCursor cursor) {
        try {
            byte[] json = jsonMapper.writeValueAsBytes(cursor);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void validate(UserInfoCursor cursor) {
        if (cursor.createdAt() == null || cursor.userInfoId() == null) {
            throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
        }
    }
}
