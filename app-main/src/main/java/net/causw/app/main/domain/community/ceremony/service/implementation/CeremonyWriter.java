package net.causw.app.main.domain.community.ceremony.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.CeremonyAttachImage;
import net.causw.app.main.domain.asset.file.repository.CeremonyAttachImageRepository;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CeremonyWriter {

	private final CeremonyRepository ceremonyRepository;
	private final CeremonyAttachImageRepository ceremonyAttachImageRepository;
	private final UuidFileService uuidFileService;

	public Ceremony save(Ceremony ceremony) {
		return ceremonyRepository.save(ceremony);
	}

	public void approve(Ceremony ceremony) {
		ceremony.approve();
	}

	public void reject(Ceremony ceremony, String rejectReason) {
		ceremony.reject();
		ceremony.updateNote(rejectReason);
	}

	public void deleteByUsers(List<User> users) {
		List<String> userIds = users.stream()
			.map(User::getId)
			.toList();

		if (userIds.isEmpty()) {
			return;
		}

		List<Ceremony> ceremonies = ceremonyRepository.findAllByUser_IdIn(userIds);
		if (ceremonies.isEmpty()) {
			return;
		}

		List<String> ceremonyIds = ceremonies.stream()
			.map(Ceremony::getId)
			.toList();

		List<CeremonyAttachImage> attachImages = ceremonyAttachImageRepository.findAllByCeremony_IdIn(ceremonyIds);

		if (!attachImages.isEmpty()) {
			List<String> fileIds = attachImages.stream()
				.map(CeremonyAttachImage::getUuidFile)
				.map(UuidFile::getId)
				.toList();

			ceremonyAttachImageRepository.deleteAll(attachImages);
			uuidFileService.deleteFileList(fileIds);
		}

		ceremonyRepository.deleteAll(ceremonies);
	}
}
