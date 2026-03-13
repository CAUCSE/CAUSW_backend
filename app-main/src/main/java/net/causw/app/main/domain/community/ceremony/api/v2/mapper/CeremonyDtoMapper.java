package net.causw.app.main.domain.community.ceremony.api.v2.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import net.causw.app.main.domain.asset.file.entity.joinEntity.CeremonyAttachImage;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyAdminListResponse;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponse;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonySummaryResponse;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;

@Mapper(componentModel = "spring")
public interface CeremonyDtoMapper {

	// 경조사 상세 보기
	@Mapping(target = "title", source = ".", qualifiedByName = "mapTitle")
	@Mapping(target = "type", source = "ceremonyType.label")
	@Mapping(target = "category", source = ".", qualifiedByName = "mapCategory")
	@Mapping(target = "applicant", source = "user.name")
	@Mapping(target = "subject", source = ".", qualifiedByName = "mapSubject")
	@Mapping(target = "content", source = "description")
	@Mapping(target = "attachedImageUrlList", source = "ceremonyAttachImageList", qualifiedByName = "mapAttachedImages")
	@Mapping(target = "isSetAll", source = "ceremony.setAll")
	@Mapping(target = "targetAdmissionYears", source = "targetAdmissionYears")
	@Mapping(target = "state", ignore = true)
	@Mapping(target = "note", ignore = true)
	CeremonyDetailResponse toDetailResponse(Ceremony ceremony);

	// 내 경조사 상세 보기
	@InheritConfiguration(name = "toDetailResponse")
	@Mapping(target = "state", source = "ceremonyState")
	@Mapping(target = "note", source = "note")
	CeremonyDetailResponse toMyDetailResponse(Ceremony ceremony);

	// 경조사 목록 조회
	@Mapping(target = "title", source = ".", qualifiedByName = "mapTitle")
	@Mapping(target = "type", source = "ceremonyType.label")
	@Mapping(target = "category", source = "ceremonyCategory.label")
	@Mapping(target = "state", ignore = true)
	CeremonySummaryResponse toSummaryResponse(Ceremony ceremony);

	// 내 경조사 목록 조회
	@InheritConfiguration(name = "toSummaryResponse")
	@Mapping(target = "state", source = "ceremonyState")
	CeremonySummaryResponse toMySummaryResponse(Ceremony ceremony);

	// 관리자 경조사 목록 조회
	@Mapping(target = "applicantName", source = "user.name")
	@Mapping(target = "applicantStudentId", source = "user.studentId")
	@Mapping(target = "state", source = "ceremonyState")
	@Mapping(target = "category", source = ".", qualifiedByName = "mapCategory")
	CeremonyAdminListResponse toAdminListResponse(Ceremony ceremony);

	// 관리자 경조사 상세 조회
	@InheritConfiguration(name = "toDetailResponse")
	@Mapping(target = "state", source = "ceremonyState")
	@Mapping(target = "note", source = "note")
	CeremonyDetailResponse toAdminDetailResponse(Ceremony ceremony);

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

	@Named("mapAttachedImages")
	default List<String> mapAttachedImages(List<CeremonyAttachImage> images) {
		return images.stream()
			.map(image -> image.getUuidFile().getFileUrl())
			.collect(Collectors.toList());
	}
}
