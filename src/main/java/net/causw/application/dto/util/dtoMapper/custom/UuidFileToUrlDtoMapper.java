package net.causw.application.dto.util.dtoMapper.custom;

import net.causw.adapter.persistence.uuidFile.joinEntity.JoinEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UuidFileToUrlDtoMapper {

    @Named("mapUuidFileToFileUrl")
    default String mapUuidFileToFileUrl (JoinEntity joinEntity) {
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
        if (joinEntityList.isEmpty()) {
            return new ArrayList<>();
        }
        return joinEntityList.stream()
                .map(mappingTable -> mappingTable.getUuidFile().getFileUrl())
                .toList();
    }

}
