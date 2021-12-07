package net.causw.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAdmissionRepository extends JpaRepository<UserAdmission, String> {
    Optional<UserAdmission> findByUser_Id(String userId);

    Boolean existsByUser_Id(String userId);

    @Query(value = "SELECT * " +
            "FROM TB_USER_ADMISSION AS ua " +
            "LEFT JOIN TB_USER AS u ON ua.user_id = u.id " +
            "WHERE u.state = :user_state ORDER BY ua.created_at DESC", nativeQuery = true)
    Page<UserAdmission> findAll(@Param("user_state") String userState, Pageable pageable);
}
