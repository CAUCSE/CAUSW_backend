package net.causw.application.spi;

import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserPort {
    Optional<UserDomainModel> findEmail(String userName, String studentId);

    Optional<UserDomainModel> findForPassword(String email, String name, String studentId);

    Optional<UserDomainModel> findById(String id);

    List<UserDomainModel> findByName(String name);

    Optional<UserDomainModel> findByEmail(String email);

    UserDomainModel create(UserDomainModel userDomainModel);

    Optional<UserDomainModel> update(String id, UserDomainModel userDomainModel);

    Optional<UserDomainModel> updateRole(String id, Role role);

    List<UserDomainModel> findByRole(Role role);

    Page<UserDomainModel> findByState(UserState state, Integer pageNum);

    Optional<UserDomainModel> updatePassword(String id, String password);

    Optional<UserDomainModel> updateState(String id, UserState state);
}
