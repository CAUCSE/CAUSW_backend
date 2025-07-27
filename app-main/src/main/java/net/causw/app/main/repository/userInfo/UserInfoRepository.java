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
    SELECT ui FROM UserInfo ui
    JOIN ui.userCareer uc
    WHERE uc.description LIKE CONCAT('%', :career, '%')
    AND ui.user.name LIKE CONCAT('%', :name, '%')
    AND ui.job LIKE CONCAT('%', :job, '%')
    ORDER BY ui.updatedAt DESC
    """)
    Page<UserInfo> findByNameAndJobAndCareer(@Param("name") String name, @Param("job") String job, @Param("career") String career, Pageable pageable);
}