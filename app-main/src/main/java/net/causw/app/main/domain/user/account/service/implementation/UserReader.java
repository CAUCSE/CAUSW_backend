package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserReader {

	private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;

	public User getUser(String userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, "사용자를 찾을 수 없습니다."));
	}

	public List<User> getUsersByIds(List<String> userIds) {
		return userQueryRepository.findByIds(userIds);
	}

	public List<User> searchByCondition(UserQueryCondition condition) {
		return userQueryRepository.searchByCondition(condition);
	}

	public Page<User> findUserList(
		UserListCondition condition,
		Pageable pageable) {
		return userQueryRepository.findUserList(
			condition.keyword(),
			condition.state(),
			condition.academicStatus(),
			condition.department(),
			pageable);
	}

	// 상세 조회용 (fetch join)
	public User findDetailById(String userId) {
		return userQueryRepository.findByIdWithRelations(userId)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

}
