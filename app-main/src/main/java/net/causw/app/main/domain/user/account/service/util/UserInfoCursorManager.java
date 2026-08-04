package net.causw.app.main.domain.user.account.service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.userinfo.SortType;
import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;
import net.causw.app.main.domain.user.account.service.dto.UserInfoCursor;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class UserInfoCursorManager {

	private final JsonMapper jsonMapper;

	public UserInfoCursor nextCursor(
		Slice<UserInfo> slice,
		UserInfoSectionType section,
		SortType sortType,
		String filterHash) {
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
			lastUserInfo.getId(),
			filterHash);
	}

	public String createFilterHash(
		UserInfoListCondition condition,
		SortType sortType,
		String excludeUserId) {
		NormalizedFilter normalizedFilter = new NormalizedFilter(
			normalizeKeyword(condition.keyword()),
			condition.admissionYearStart(),
			condition.admissionYearEnd(),
			normalizeAcademicStatuses(condition.academicStatus()),
			sortType,
			excludeUserId);

		try {
			byte[] serializedFilter = jsonMapper.writeValueAsBytes(normalizedFilter);
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(serializedFilter);
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm is not available.", e);
		}
	}

	public void validateFilterHash(
		UserInfoCursor cursor,
		UserInfoListCondition condition,
		SortType sortType,
		String excludeUserId) {
		String currentFilterHash = createFilterHash(condition, sortType, excludeUserId);
		if (!MessageDigest.isEqual(
			cursor.filterHash().getBytes(StandardCharsets.UTF_8),
			currentFilterHash.getBytes(StandardCharsets.UTF_8))) {
			throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
		}
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
			|| cursor.sortType() == null
			|| cursor.filterHash() == null
			|| cursor.filterHash().isBlank()) {
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

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return "";
		}
		return keyword.toLowerCase(Locale.ROOT);
	}

	private List<String> normalizeAcademicStatuses(List<String> academicStatuses) {
		List<AcademicStatus> normalizedStatuses;
		if (academicStatuses == null || academicStatuses.isEmpty()) {
			normalizedStatuses = List.of(AcademicStatus.ENROLLED, AcademicStatus.GRADUATED);
		} else {
			normalizedStatuses = academicStatuses.stream()
				.map(AcademicStatus::fromString)
				.distinct()
				.toList();
		}

		List<String> statusNames = new ArrayList<>(normalizedStatuses.stream()
			.map(AcademicStatus::name)
			.toList());
		statusNames.sort(String::compareTo);
		return List.copyOf(statusNames);
	}

	private record NormalizedFilter(
		String keyword,
		Integer admissionYearStart,
		Integer admissionYearEnd,
		List<String> academicStatuses,
		SortType sortType,
		String excludeUserId) {
	}
}
