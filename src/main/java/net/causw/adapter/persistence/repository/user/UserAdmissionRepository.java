package net.causw.adapter.persistence.repository.user;

import net.causw.adapter.persistence.user.UserAdmission;
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
            "FROM tb_user_admission AS ua " +
            "LEFT JOIN tb_user AS u ON ua.user_id = u.id " +
            "WHERE u.state = :user_state ORDER BY ua.created_at DESC", nativeQuery = true)
    Page<UserAdmission> findAll(@Param("user_state") String userState, Pageable pageable);

    @Query(value = "SELECT ua.id AS id, ua.user_id,ua.image,ua.reject_reason,ua.description,ua.updated_at,ua.created_at, u.name, u.state  " +
            "FROM tb_user_admission AS ua " +
            "LEFT JOIN tb_user AS u ON ua.user_id = u.id " +
            "WHERE u.state = :user_state AND (:name IS NULL OR u.name LIKE %:name%) ORDER BY ua.created_at DESC", nativeQuery = true)
    Page<UserAdmission> findAllWithName(@Param("user_state") String userState, @Param("name") String name, Pageable pageable);
}