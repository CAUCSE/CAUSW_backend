package net.causw.adapter.persistence.repository.userInfo;

import java.util.List;
import net.causw.adapter.persistence.userInfo.UserCareer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCareerRepository extends JpaRepository<UserCareer, String> {

  List<UserCareer> findAllCareerByUserInfoId(String userInfoId);
}
