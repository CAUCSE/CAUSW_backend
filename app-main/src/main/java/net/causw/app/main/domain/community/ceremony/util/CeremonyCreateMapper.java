package net.causw.app.main.domain.community.ceremony.util;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.user.account.entity.user.User;

@Component
public class CeremonyCreateMapper {

	public Ceremony fromRequest(
		User user,
		CreateCeremonyRequestDto dto,
		List<String> targetAdmissionYears,
		List<UuidFile> uuidFileList) {
		return Ceremony.createWithImages(
			user,
			dto.getCeremonyType(),
			dto.getCeremonyCategory(),
			dto.getCeremonyCustomCategory(),
			dto.getStartDate(),
			dto.getEndDate(),
			dto.getStartTime(),
			dto.getEndTime(),
			dto.getRelationType(),
			dto.getFamilyRelation(),
			dto.getAlumniRelation(),
			dto.getAlumniName(),
			dto.getAlumniAdmissionYear(),
			dto.getContent(),
			dto.getAddress(),
			dto.getPostalAddress(),
			dto.getDetailedAddress(),
			dto.getContact(),
			dto.getLink(),
			dto.getIsSetAll(),
			targetAdmissionYears,
			uuidFileList);
	}
}
