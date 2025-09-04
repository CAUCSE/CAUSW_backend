package net.causw.app.main.dto.util.dtoMapper.custom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.JoinEntity;

@Mapper(componentModel = "spring")
public interface UuidFileToUrlDtoMapper {

	@Named("mapUuidFileToFileUrl")
	default String mapUuidFileToFileUrl(JoinEntity joinEntity) {
		if (joinEntity == null) {
			return null;
		} else {
			return joinEntity.getUuidFile() == null ?
				null
				: joinEntity.getUuidFile().getFileUrl();
		}
	}

	@Named(value = "mapUuidFileListToFileUrlList")
	default List<String> mapUuidFileListToFileUrlList(List<? extends JoinEntity> joinEntityList) {

		if (joinEntityList == null || joinEntityList.isEmpty()) {
			return new ArrayList<>();
		}
		return joinEntityList.stream()
			.sorted(Comparator.comparing(JoinEntity::getCreatedAt))
			.map(mappingTable -> Optional.ofNullable(mappingTable.getUuidFile())
				.map(UuidFile::getFileUrl)
				.orElse(null))
			.filter(Objects::nonNull)
			.toList();
	}

}
