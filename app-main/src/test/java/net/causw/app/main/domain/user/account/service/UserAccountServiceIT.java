package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountReader;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserAccountServiceIT {

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private UserRepository userRepository;

	@MockBean
	private SocialAccountReader socialAccountReader;

	@MockBean
	private LockerReader lockerReader;

	@Test
	@DisplayName("탈퇴 시 deletedAt이 저장된다")
	void withdraw_updatesDeletedAt() {
		// given
		UserRegisterDto dto = new UserRegisterDto(
			"test@test.com",
			"password123",
			"홍길동",
			"닉네임",
			"01012345678",
			"ABCD12",
			List.of());

		User user = userRepository.save(User.from(dto, "encodedPassword"));

		BDDMockito.given(socialAccountReader.findAllByUserId(user.getId()))
			.willReturn(List.of()); // 소셜 계정이 없는 케이스로 설정

		// 사물함 없음
		BDDMockito.given(lockerReader.findByUserId(user.getId()))
			.willReturn(Optional.empty());

		// when
		userAccountService.withdraw(user.getId(), "access", "refresh");

		// then
		User found = userRepository.findById(user.getId()).orElseThrow();
		assertThat(found.getDeletedAt()).isNotNull();
	}
}