package net.causw.application.dto.util.dtoMapper;


import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.uuidFile.joinEntity.CeremonyAttachImage;
import net.causw.application.dto.ceremony.CeremonyNotificationSettingResponseDto;
import net.causw.application.dto.ceremony.CeremonyResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CeremonyDtoMapper {

    CeremonyDtoMapper INSTANCE = Mappers.getMapper(CeremonyDtoMapper.class);

    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "category", source = "ceremonyCategory")
    @Mapping(target = "ceremonyState", source = "ceremonyState")
    @Mapping(target = "attachedImageUrlList", source = "ceremonyAttachImageList", qualifiedByName = "mapAttachedImages")
    CeremonyResponseDto toCeremonyResponseDto(Ceremony ceremony);



    @Mapping(target = "isNotificationActive", source = "notificationActive")
    @Mapping(target = "isSetAll", source = "setAll")
    @Mapping(target = "subscribedAdmissionYears", source = "subscribedAdmissionYears")
    CeremonyNotificationSettingResponseDto toCeremonyNotificationSettingResponseDto(CeremonyNotificationSetting ceremonyNotificationSetting);

    @Named("mapAttachedImages")
    default List<String> mapAttachedImages(List<CeremonyAttachImage> images) {
        return images.stream()
                .map(image -> image.getUuidFile().getFileUrl())
                .collect(Collectors.toList());
    }
}
