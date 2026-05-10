package net.causw.app.main.domain.user.account.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.user.account.entity.user.DroppedUserIdentifier;
import net.causw.app.main.domain.user.account.enums.user.DroppedIdentifierType;

public interface DroppedUserIdentifierRepository extends JpaRepository<DroppedUserIdentifier, Long> {

	boolean existsByIdentifierTypeAndIdentifierHash(DroppedIdentifierType identifierType, String identifierHash);

	List<DroppedUserIdentifier> findAllByUserId(String userId);
}
