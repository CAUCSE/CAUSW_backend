package net.causw.app.main.dto.util.dtoMapper;


import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CeremonyAttachImage;
import net.causw.app.main.dto.ceremony.CeremonyNotificationSettingResponseDto;
import net.causw.app.main.dto.ceremony.CeremonyResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CeremonyDtoMapper {

    CeremonyDtoMapper INSTANCE = Mappers.getMapper(CeremonyDtoMapper.class);

    // general
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "category", source = "ceremonyCategory")
    @Mapping(target = "ceremonyState", source = "ceremonyState")
    @Mapping(target = "attachedImageUrlList", source = "ceremonyAttachImageList", qualifiedByName = "mapAttachedImages")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "applicantStudentId", source = "user.studentId")
    @Mapping(target = "applicantName", source = "user.name")
    @Mapping(target = "title", source = ".", qualifiedByName = "mapTitle")
    @Mapping(target = "isSetAll", ignore = true)    // general에서는 숨김
    @Mapping(target = "targetAdmissionYears", ignore = true)    // general에서는 숨김
    CeremonyResponseDto toCeremonyResponseDto(Ceremony ceremony);

    // admin, my
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "category", source = "ceremonyCategory")
    @Mapping(target = "ceremonyState", source = "ceremonyState")
    @Mapping(target = "attachedImageUrlList", source = "ceremonyAttachImageList", qualifiedByName = "mapAttachedImages")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "applicantStudentId", source = "user.studentId")
    @Mapping(target = "applicantName", source = "user.name")
    @Mapping(target = "title", source = ".", qualifiedByName = "mapTitle")
    @Mapping(target = "isSetAll", source = "ceremony.setAll")  // 상세 조회에서는 표시
    @Mapping(target = "targetAdmissionYears", source = "targetAdmissionYears")  // 상세 조회에서는 표시
    CeremonyResponseDto toDetailedCeremonyResponseDto(Ceremony ceremony);

    @Named("mapTitle")
    static String mapTitle(Ceremony ceremony) {
        return String.format("%s(%s) - %s",
                ceremony.getUser().getName(),
                ceremony.getUser().getAdmissionYear().toString(),
                ceremony.getCeremonyCategory().getLabel());
    }



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
