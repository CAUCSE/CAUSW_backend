package net.causw.app.main.dto.util.dtoMapper.custom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FormatDateTimeDtoMapper {

	@Named("formatDateTime")
	default String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null)
			return null;
		return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd."));
	}

}
