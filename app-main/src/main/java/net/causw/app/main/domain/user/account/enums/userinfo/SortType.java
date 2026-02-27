package net.causw.app.main.domain.user.account.enums.userinfo;

import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortType {
	UPDATED_AT_ASC, // 수정된 시간 오름차순
	UPDATED_AT_DESC, // 수정된 시간 내림차순
	ADMISSION_YEAR_ASC, // 학번 오름차순
	ADMISSION_YEAR_DESC; // 학번 내림차순

	public static SortType fromString(String sortType) {
		if (sortType == null || sortType.isEmpty()) {
			throw UserInfoErrorCode.INVALID_SORT_TYPE.toBaseException();
		}
		for (SortType type : SortType.values()) {
			if (type.name().equalsIgnoreCase(sortType)) {
				return type;
			}
		}
		throw UserInfoErrorCode.INVALID_SORT_TYPE.toBaseException();
	}
}
