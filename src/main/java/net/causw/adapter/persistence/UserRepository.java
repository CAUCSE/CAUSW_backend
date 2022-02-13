package net.causw.adapter.persistence;

import net.causw.domain.model.Role;
import net.causw.domain.model.UserState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    List<User> findByName(String name);

    List<User> findByRole(Role role);

    Page<User> findByStateOrderByCreatedAtAsc(UserState state, Pageable pageable);
}
