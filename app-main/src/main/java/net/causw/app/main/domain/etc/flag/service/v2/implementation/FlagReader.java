package net.causw.app.main.domain.etc.flag.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.etc.flag.repository.FlagRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlagReader {

	private final FlagRepository flagRepository;

	public boolean findValueByKey(String key) {
		return flagRepository.findByKey(key)
			.map(flag -> Boolean.TRUE.equals(flag.getValue()))
			.orElse(false);
	}
}
