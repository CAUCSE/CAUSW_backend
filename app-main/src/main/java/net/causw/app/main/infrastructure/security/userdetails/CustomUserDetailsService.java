package net.causw.app.main.infrastructure.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.repository.user.query.UserQueryRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;
	private final UserQueryRepository userQueryRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return CustomUserDetails.builder()
			.user(user)
			.build();
	}

	public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
		User user = userQueryRepository.findByIdWithRoles(userId)
			.orElseThrow(() -> new BadRequestException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.LOGIN_USER_NOT_FOUND
				)
			);

		return CustomUserDetails.builder()
			.user(user)
			.build();
	}
}

