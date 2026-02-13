package net.causw.app.main.domain.community.ceremony.api.v2.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.asset.file.entity.joinEntity.CeremonyAttachImage;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyNotificationSettingResponseDto;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;

@Mapper(componentModel = "spring")
public interface CeremonyDtoMapper {

	CeremonyDtoMapper INSTANCE = Mappers.getMapper(CeremonyDtoMapper.class);

	// 경조사 상세 보기
	@Mapping(target = "id", source = "id")
	@Mapping(target = "title", source = ".", qualifiedByName = "mapTitle")
	@Mapping(target = "type", source = "ceremonyType.label")
	@Mapping(target = "category", source = ".", qualifiedByName = "mapCategory")
	@Mapping(target = "startDate", source = "startDate")
	@Mapping(target = "endDate", source = "endDate")
	@Mapping(target = "startTime", source = "startTime")
	@Mapping(target = "endTime", source = "endTime")
	@Mapping(target = "applicant", source = "user.name")
	@Mapping(target = "subject", source = ".", qualifiedByName = "mapSubject")
	@Mapping(target = "content", source = "description")
	@Mapping(target = "attachedImageUrlList", source = "ceremonyAttachImageList", qualifiedByName = "mapAttachedImages")
	@Mapping(target = "address", source = "address")
	@Mapping(target = "postalAddress", source = "postalAddress")
	@Mapping(target = "detailedAddress", source = "detailedAddress")
	@Mapping(target = "contact", source = "contact")
	@Mapping(target = "link", source = "link")
	@Mapping(target = "isSetAll", source = "ceremony.setAll")
	@Mapping(target = "targetAdmissionYears", source = "targetAdmissionYears")
	@Mapping(target = "state", ignore = true)
	@Mapping(target = "note", ignore = true)
	CeremonyDetailResponseDto toDetailedCeremonyResponseDto(Ceremony ceremony);

	// 내 경조사 상세 보기
	@Mapping(target = "id", source = "id")
	@Mapping(target = "title", source = ".", qualifiedByName = "mapTitle")
	@Mapping(target = "type", source = "ceremonyType.label")
	@Mapping(target = "category", source = ".", qualifiedByName = "mapCategory")
	@Mapping(target = "startDate", source = "startDate")
	@Mapping(target = "endDate", source = "endDate")
	@Mapping(target = "startTime", source = "startTime")
	@Mapping(target = "endTime", source = "endTime")
	@Mapping(target = "applicant", source = "user.name")
	@Mapping(target = "subject", source = ".", qualifiedByName = "mapSubject")
	@Mapping(target = "content", source = "description")
	@Mapping(target = "attachedImageUrlList", source = "ceremonyAttachImageList", qualifiedByName = "mapAttachedImages")
	@Mapping(target = "address", source = "address")
	@Mapping(target = "postalAddress", source = "postalAddress")
	@Mapping(target = "detailedAddress", source = "detailedAddress")
	@Mapping(target = "contact", source = "contact")
	@Mapping(target = "link", source = "link")
	@Mapping(target = "isSetAll", source = "ceremony.setAll")
	@Mapping(target = "targetAdmissionYears", source = "targetAdmissionYears")
	@Mapping(target = "state", source = "ceremonyState")
	@Mapping(target = "note", source = "note")
	CeremonyDetailResponseDto toMyCeremonyDetailResponseDto(Ceremony ceremony);

	@Named("mapCategory")
	static String mapCategory(Ceremony ceremony) {
		if (ceremony.getCeremonyCategory() == CeremonyCategory.ETC) {
			return ceremony.getCeremonyCustomCategory();
		} else {
			return ceremony.getCeremonyCategory().getLabel();
		}
	}

	@Named("mapTitle")
	static String mapTitle(Ceremony ceremony) {
		switch (ceremony.getRelationType()) {
			case FAMILY -> {
				return String.format("%s(%s학번) %s %s",
					ceremony.getUser().getName(),
					ceremony.getUser().getAdmissionYear().toString().substring(2, 4),
					ceremony.getFamilyRelation(),
					mapCategory(ceremony));
			}
			case INSTEAD -> {
				if (ceremony.getAlumniRelation().equals("본인")) {
					return String.format("%s(%s학번) %s",
						ceremony.getAlumniName(),
						ceremony.getAlumniAdmissionYear().substring(2, 4),
						mapCategory(ceremony));
				} else {
					return String.format("%s(%s학번) %s %s",
						ceremony.getAlumniName(),
						ceremony.getAlumniAdmissionYear().substring(2, 4),
						ceremony.getAlumniRelation(),
						mapCategory(ceremony));
				}
			}
			default -> {
				return String.format("%s(%s학번) %s",
					ceremony.getUser().getName(),
					ceremony.getUser().getAdmissionYear().toString().substring(2, 4),
					mapCategory(ceremony));
			}
		}
	}

	@Named("mapSubject")
	static String mapSubject(Ceremony ceremony) {
		switch (ceremony.getRelationType()) {
			case FAMILY -> {
				return String.format("%s %s",
					ceremony.getUser().getName(),
					ceremony.getFamilyRelation());
			}
			case INSTEAD -> {
				if (ceremony.getAlumniRelation().equals("본인")) {
					return ceremony.getAlumniName();
				} else {
					return String.format("%s %s",
						ceremony.getAlumniName(),
						ceremony.getAlumniRelation());
				}
			}
			default -> {
				return ceremony.getUser().getName();
			}
		}
	}

	@Mapping(target = "isNotificationActive", source = "notificationActive")
	@Mapping(target = "isSetAll", source = "setAll")
	@Mapping(target = "subscribedAdmissionYears", source = "subscribedAdmissionYears")
	CeremonyNotificationSettingResponseDto toCeremonyNotificationSettingResponseDto(
		CeremonyNotificationSetting ceremonyNotificationSetting);

	@Named("mapAttachedImages")
	default List<String> mapAttachedImages(List<CeremonyAttachImage> images) {
		return images.stream()
			.map(image -> image.getUuidFile().getFileUrl())
			.collect(Collectors.toList());
	}
}
