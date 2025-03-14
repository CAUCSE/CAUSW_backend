package net.causw.adapter.persistence.repository.user;

import net.causw.adapter.persistence.user.UserAdmission;
import org.jetbrains.annotations.NotNull;
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

    @Query(value = "SELECT ua.id AS id, ua.user_id,uai.uuid_file_id AS user_admission_attach_image ,ua.description,ua.updated_at,ua.created_at, u.name, u.state  " +
            "FROM tb_user_admission AS ua " +
            "LEFT JOIN tb_user AS u ON ua.user_id = u.id " +
            "LEFT JOIN tb_user_admission_attach_image_uuid_file AS uai ON ua.id = uai.user_admission_id " +
            "WHERE u.state = :user_state AND (:name IS NULL OR u.name LIKE %:name%) ORDER BY ua.created_at DESC", nativeQuery = true)
    Page<UserAdmission> findAllWithStateAneName(@Param("user_state") String userState, @Param("name") String name, Pageable pageable);

    @NotNull Page<UserAdmission> findAll(@NotNull Pageable pageable);

    Page<UserAdmission> findAllByUserName(String name, Pageable pageable);

}