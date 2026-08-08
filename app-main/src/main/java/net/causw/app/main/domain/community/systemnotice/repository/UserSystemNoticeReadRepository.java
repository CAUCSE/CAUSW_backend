package net.causw.app.main.domain.community.systemnotice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;

@Repository
public interface UserSystemNoticeReadRepository extends JpaRepository<UserSystemNoticeRead, String> {

	Optional<UserSystemNoticeRead> findByUserId(String userId);
}
