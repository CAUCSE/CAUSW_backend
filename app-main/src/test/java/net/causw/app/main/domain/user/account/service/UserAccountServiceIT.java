package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserRegisterDto;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.shared.infra.firebase.FcmUtils;
import net.causw.app.main.shared.infra.firebase.FirebaseConfig;

@SpringBootTest
@Transactional
class UserAccountServiceIT {

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private UserRepository userRepository;

	@MockBean
	private SocialAccountWriter socialAccountWriter;

	@MockBean
	private LockerReader lockerReader;

	@MockBean
	private LockerWriter lockerWriter;

	@MockBean
	private FcmUtils fcmUtils;

	@MockBean
	private FirebaseConfig firebaseConfig;

	@MockBean
	private RedisTemplate<String, String> redisTemplate;

	@MockBean
	private StringRedisTemplate stringRedisTemplate;

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
			"ABCD12");

		User user = userRepository.save(User.from(dto, "encodedPassword"));

		// 사물함 없음
		org.mockito.BDDMockito.given(lockerReader.findByUserId(user.getId()))
			.willReturn(Optional.empty());

		// when
		userAccountService.withdraw(user.getId(), "access", "refresh");

		// then
		User found = userRepository.findById(user.getId()).orElseThrow();
		assertThat(found.getDeletedAt()).isNotNull();
	}
}