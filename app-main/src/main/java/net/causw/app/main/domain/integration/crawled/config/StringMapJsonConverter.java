package net.causw.app.main.domain.integration.crawled.config;

import java.util.Map;

import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Converter
public class StringMapJsonConverter implements AttributeConverter<Map<String, String>, String> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

	/**
	 * 요청 헤더 맵을 데이터베이스 저장용 JSON 문자열로 변환합니다.
	 *
	 * @param attribute 변환할 요청 헤더 맵. {@code null}이면 빈 맵으로 처리합니다.
	 * @return 요청 헤더를 직렬화한 JSON 문자열
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception JSON 직렬화에 실패한 경우
	 */
	@Override
	public String convertToDatabaseColumn(Map<String, String> attribute) {
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute == null ? Map.of() : attribute);
		} catch (Exception e) {
			throw IntegrationErrorCode.CRAWLING_ERROR.toBaseException("크롤링 요청 헤더를 JSON으로 직렬화하지 못했습니다.", e);
		}
	}

	/**
	 * 데이터베이스에 저장된 JSON 문자열을 요청 헤더 맵으로 변환합니다.
	 *
	 * @param dbData 변환할 JSON 문자열. {@code null} 또는 빈 문자열이면 빈 맵을 반환합니다.
	 * @return 수정할 수 없는 요청 헤더 맵
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception JSON 역직렬화에 실패한 경우
	 */
	@Override
	public Map<String, String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return Map.of();
		}
		try {
			return Map.copyOf(OBJECT_MAPPER.readValue(dbData, MAP_TYPE));
		} catch (Exception e) {
			throw IntegrationErrorCode.CRAWLING_ERROR.toBaseException("크롤링 요청 헤더를 JSON으로 역직렬화하지 못했습니다.", e);
		}
	}
}
