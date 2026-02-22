package net.causw.app.main.domain.community.ceremony.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CeremonyWriter {

	private final CeremonyRepository ceremonyRepository;

	public Ceremony save(Ceremony ceremony) {
		return ceremonyRepository.save(ceremony);
	}

	public void approve(Ceremony ceremony) {
		ceremony.approve();
		ceremonyRepository.save(ceremony);
	}

	public void reject(Ceremony ceremony, String rejectReason) {
		ceremony.reject();
		ceremony.updateNote(rejectReason);
		ceremonyRepository.save(ceremony);
	}
}
