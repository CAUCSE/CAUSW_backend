package net.causw.app.main.domain.user.account.service.implementation;


import lombok.RequiredArgsConstructor;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserQueryRepository userQueryRepository;

    public Page<User> findUserList(
            UserListCondition condition,
            Pageable pageable
    ) {
        return userQueryRepository.findUserList(
                condition.keyword(),
                condition.state(),
                condition.academicStatus(),
                condition.department(),
                pageable
        );
    }
}
