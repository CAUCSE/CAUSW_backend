package net.causw.app.main.repository.userInfo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.userInfo.UserCareer;

@Repository
public interface UserCareerRepository extends JpaRepository<UserCareer, String> {

	List<UserCareer> findAllCareerByUserInfoId(String userInfoId);
}
