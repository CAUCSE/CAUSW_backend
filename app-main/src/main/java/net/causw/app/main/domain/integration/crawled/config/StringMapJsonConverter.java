package net.causw.app.main.domain.integration.crawled.config;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringMapJsonConverter implements AttributeConverter<Map<String, String>, String> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

	@Override
	public String convertToDatabaseColumn(Map<String, String> attribute) {
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute == null ? Map.of() : attribute);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to serialize crawl request headers", e);
		}
	}

	@Override
	public Map<String, String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return Map.of();
		}
		try {
			return Map.copyOf(OBJECT_MAPPER.readValue(dbData, MAP_TYPE));
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to deserialize crawl request headers", e);
		}
	}
}
