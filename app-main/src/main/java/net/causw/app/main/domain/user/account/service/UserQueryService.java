package net.causw.app.main.domain.user.account.service;

import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserReader userReader;

    public Page<UserListItem> getUserList(
            UserListCondition condition,
            Pageable pageable
    ) {
        return userReader.findUserList(condition, pageable)
                .map(UserListItem::from);
    }
}
