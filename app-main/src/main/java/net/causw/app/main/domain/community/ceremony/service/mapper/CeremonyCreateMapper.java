package net.causw.app.main.domain.community.ceremony.service.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyCreateCommand;
import net.causw.app.main.domain.user.account.entity.user.User;

@Component
public class CeremonyCreateMapper {

	public Ceremony fromRequest(
		User user,
		CeremonyCreateCommand command,
		List<String> targetAdmissionYears,
		List<UuidFile> uuidFileList) {
		return Ceremony.createWithImages(
			user,
			command.ceremonyType(),
			command.ceremonyCategory(),
			command.ceremonyCustomCategory(),
			command.startDate(),
			command.endDate(),
			command.startTime(),
			command.endTime(),
			command.relationType(),
			command.familyRelation(),
			command.alumniRelation(),
			command.alumniName(),
			command.alumniAdmissionYear(),
			command.content(),
			command.address(),
			command.postalAddress(),
			command.detailedAddress(),
			command.contact(),
			command.link(),
			command.isSetAll(),
			targetAdmissionYears,
			uuidFileList);
	}
}
