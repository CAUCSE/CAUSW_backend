package net.causw.app.main.service.user;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.repository.user.query.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserEntityService {

	private final UserQueryRepository userQueryRepository;

	public List<User> findAllActiveUsersByRoles(List<Role> roles) {
		return userQueryRepository.findAllActiveUsersByRoles(roles);
	}
}
