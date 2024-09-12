package net.causw.application.dto.util.dtoMapper.custom;

import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UuidFileToUrlDtoMapper {

    @Named("mapUuidFileToFileUrl")
    default String mapUuidFileToFileUrl(UuidFile uuidFile) {
        if (uuidFile == null) {
            return null;
        }
        return uuidFile.getFileUrl();
    }

    @Named("mapUuidFileListToFileUrlList")
    default List<String> mapUuidFileListToFileUrlList(List<UuidFile> uuidFileList) {
        if (uuidFileList.isEmpty()) {
            return new ArrayList<>();
        }
        return uuidFileList.stream()
                .map(UuidFile::getFileUrl)
                .toList();
    }

}
