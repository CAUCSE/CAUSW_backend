package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.repository.user.UserAdmissionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdmissionReader {

	private final UserAdmissionRepository userAdmissionRepository;

	/**
	 * 사용자 ID로 현재 UserAdmission을 조회합니다.
	 */
	public Optional<UserAdmission> findByUserId(String userId) {
		return userAdmissionRepository.findByUser_Id(userId);
	}

	/**
	 * 사용자에게 이미 신청이 존재하는지 확인합니다.
	 */
	public boolean existsByUserId(String userId) {
		return userAdmissionRepository.existsByUser_Id(userId);
	}
}
