package net.causw.app.main.domain.model.entity.userInfo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class SocialLinksConverter implements AttributeConverter<List<String>, String> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return "[]";
		}

		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			log.error("socialLink-convert-to-json-error", e);
			return "[]";
		}
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.trim().isEmpty()) {
			return new ArrayList<>();
		}

		try {
			return objectMapper.readValue(dbData, new TypeReference<List<String>>() {
			});
		} catch (JsonProcessingException e) {
			log.error("socialLink-convert-to-attribute-error", e);
			return new ArrayList<>();
		}
	}
}
