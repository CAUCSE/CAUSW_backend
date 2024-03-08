package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findAll();

    Optional<User> findByEmailAndNameAndStudentId(String email, String name, String studentId);

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    List<User> findByName(String name);

    List<User> findByRoleAndState(Role role, UserState state);

    @Query(value = "SELECT * "  +
            "FROM tb_user AS u " +
            "WHERE u.state = :state AND (:name IS NULL OR u.name LIKE %:name%) ORDER BY u.created_at DESC" , nativeQuery = true)
    Page<User> findByStateAndName(@Param("state") String state, @Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * "  +
            "FROM tb_user AS u " +
            "WHERE u.state IN :state AND (COALESCE(:name, '') = '' OR u.name LIKE CONCAT('%', :name, '%')) ORDER BY u.created_at DESC" , nativeQuery = true)
    Page<User> findByStateInAndNameContaining(@Param("state")List<String> states, @Param("name")String name, Pageable pageable);
}












