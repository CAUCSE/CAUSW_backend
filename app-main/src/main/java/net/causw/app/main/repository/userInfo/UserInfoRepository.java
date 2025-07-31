package net.causw.app.main.repository.userInfo;

import java.util.Optional;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    Optional<UserInfo> findByUserId(String userId);

    Page<UserInfo> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("""
    SELECT DISTINCT ui FROM UserInfo ui
    LEFT JOIN ui.userCareer uc
    WHERE uc.description LIKE CONCAT('%', :keyword, '%')
    OR ui.user.name LIKE CONCAT('%', :keyword, '%')
    OR ui.job LIKE CONCAT('%', :keyword, '%')
    ORDER BY ui.updatedAt DESC
    """)
    Page<UserInfo> findAllByKeywordInNameOrJobOrCareer(@Param("keyword") String keyword, Pageable pageable);
}