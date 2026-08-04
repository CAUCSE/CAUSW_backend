package net.causw.app.main.domain.user.account.service.util;

import java.util.Base64;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.userinfo.SortType;
import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.service.dto.UserInfoCursor;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class UserInfoCursorManager {

	private final JsonMapper jsonMapper;

	public UserInfoCursor nextCursor(Slice<UserInfo>slice, UserInfoSectionType section, SortType sortType) {
		if (!slice.hasNext() || slice.getContent().isEmpty()) {
			return null;
		}

		UserInfo lastUserInfo = slice.getContent().getLast();
		User lastUser = lastUserInfo.getUser();
		return new UserInfoCursor(
				section,
				sortType,
				lastUserInfo.getUpdatedAt(),
				lastUser.getAdmissionYear(),
				lastUser.getName(),
				lastUserInfo.getId());
	}

	public UserInfoCursor decode(String cursor) {
		try {
			byte[] json = Base64.getUrlDecoder()
				.decode(cursor);

			UserInfoCursor decodedCursor = jsonMapper.readValue(json, UserInfoCursor.class);

			validate(decodedCursor);
			return decodedCursor;
		} catch (Exception e) {
			throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
		}
	}

	public String encode(UserInfoCursor cursor) {
		try {
			validate(cursor);
			byte[] json = jsonMapper.writeValueAsBytes(cursor);

			return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(json);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void validate(UserInfoCursor cursor) {
		if (cursor == null
			|| cursor.section() == null
			|| cursor.sortType() == null) {
			throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
		}

		boolean sectionStart = cursor.updatedAt() == null
			&& cursor.admissionYear() == null
			&& cursor.name() == null
			&& cursor.userInfoId() == null;
		boolean itemPosition = cursor.updatedAt() != null
			&& cursor.admissionYear() != null
			&& cursor.name() != null
			&& cursor.userInfoId() != null;

		if (!sectionStart && !itemPosition) {
			throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
		}
	}
}
