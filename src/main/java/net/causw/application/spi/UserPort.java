package net.causw.application.spi;

import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserPort {

    Optional<UserDomainModel> findForPassword(String email, String name, String studentId);

    Optional<UserDomainModel> findById(String id);

    List<UserDomainModel> findByName(String name);

    Optional<UserDomainModel> findByEmail(String email);

    Optional<UserDomainModel> findByRefreshToken(String refreshToken);

    UserDomainModel create(UserDomainModel userDomainModel);

    Optional<UserDomainModel> update(String id, UserDomainModel userDomainModel);

    Optional<UserDomainModel> updateRole(String id, Role role);

    Optional<UserDomainModel> removeRole(String id, Role role);

    List<UserDomainModel> findByRole(String role);

    Page<UserDomainModel> findByStateAndName(String state, String name, Integer pageNum);

    Optional<UserDomainModel> updatePassword(String id, String password);

    Optional<UserDomainModel> updateState(String id, UserState state);

    void updateRefreshToken(String id, String refreshToken);

    String getUserIdFromRefreshToken(String refreshToken);

    void signOut(String refreshToken, String accessToken);
}
