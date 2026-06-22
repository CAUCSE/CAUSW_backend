package net.causw.app.main.domain.user.account.service.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.enums.user.UserAdmissionLogAction;
import net.causw.app.main.domain.user.account.repository.user.UserAdmissionLogRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdmissionLogReader {

	private final UserAdmissionLogRepository userAdmissionLogRepository;

	public Long countAcceptedBetween(LocalDateTime start, LocalDateTime end) {
		return userAdmissionLogRepository.countByActionAndCreatedAtBetween(
			UserAdmissionLogAction.ACCEPT, start, end);
	}
}
