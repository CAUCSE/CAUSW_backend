package net.causw.app.main.domain.community.ceremony.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CeremonyReader {

	private final CeremonyRepository ceremonyRepository;

	public Optional<Ceremony> findById(String ceremonyId) {
		return ceremonyRepository.findById(ceremonyId);
	}
}
