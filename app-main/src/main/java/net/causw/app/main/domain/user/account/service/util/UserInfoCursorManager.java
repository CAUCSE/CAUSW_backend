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

	/**
	 * 조회 결과의 마지막 항목을 기준으로 다음 페이지 커서를 생성한다.
	 * @param slice 현재 조회 결과
	 * @param section 현재 조회한 섹션
	 * @param sortType 정렬 기준
	 * @param filterHash 조회 조건 식별 hash
	 * @return 다음 페이지 커서, 다음 페이지가 없으면 {@code null}
	 */
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

	/**
	 * 검색 조건과 사용자별 제외 조건을 정규화하여 SHA-256 hash를 생성한다.
	 * @param condition 검색 및 필터 조건
	 * @param sortType 적용할 정렬 기준
	 * @param excludeUserId 조회 결과에서 제외할 사용자 ID
	 * @return 정규화된 조회 조건의 SHA-256 hash
	 */
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

	/**
	 * 커서 발급 당시의 조회 조건과 현재 요청의 조회 조건이 같은지 검증한다.
	 * @param cursor 검증할 커서
	 * @param condition 현재 요청의 검색 및 필터 조건
	 * @param sortType 현재 요청에 적용할 정렬 기준
	 * @param excludeUserId 조회 결과에서 제외할 사용자 ID
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 조회 조건이 다르면 발생
	 */
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

	/**
	 * Base64 URL-safe 문자열을 동문 목록 커서로 변환하고 필수 값을 검증한다.
	 * @param cursor 인코딩된 커서 문자열
	 * @return 디코딩된 동문 목록 커서
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 커서 형식이 올바르지 않으면 발생
	 */
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

	/**
	 * 동문 목록 커서를 Base64 URL-safe 문자열로 변환한다.
	 * @param cursor 인코딩할 동문 목록 커서
	 * @return 인코딩된 커서 문자열
	 */
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
