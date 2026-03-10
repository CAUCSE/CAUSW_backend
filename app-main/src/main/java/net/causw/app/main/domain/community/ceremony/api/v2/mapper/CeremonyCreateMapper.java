package net.causw.app.main.domain.community.ceremony.api.v2.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequest;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.user.account.entity.user.User;

@Component
public class CeremonyCreateMapper {

	public Ceremony fromRequest(
		User user,
		CreateCeremonyRequest dto,
		List<String> targetAdmissionYears,
		List<UuidFile> uuidFileList) {
		return Ceremony.createWithImages(
			user,
			dto.ceremonyType(),
			dto.ceremonyCategory(),
			dto.ceremonyCustomCategory(),
			dto.startDate(),
			dto.endDate(),
			dto.startTime(),
			dto.endTime(),
			dto.relationType(),
			dto.familyRelation(),
			dto.alumniRelation(),
			dto.alumniName(),
			dto.alumniAdmissionYear(),
			dto.content(),
			dto.address(),
			dto.postalAddress(),
			dto.detailedAddress(),
			dto.contact(),
			dto.link(),
			dto.isSetAll(),
			targetAdmissionYears,
			uuidFileList);
	}
}
