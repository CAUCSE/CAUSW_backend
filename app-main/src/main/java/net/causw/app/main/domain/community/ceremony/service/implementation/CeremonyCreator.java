package net.causw.app.main.domain.community.ceremony.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CeremonyCreator {

	private final CeremonyRepository ceremonyRepository;

	public Ceremony save(Ceremony ceremony) {
		return ceremonyRepository.save(ceremony);
	}
}
