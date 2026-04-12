package net.causw.app.main.domain.user.account.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.user.account.entity.user.BlockedUserIdentifier;
import net.causw.app.main.domain.user.account.enums.user.BlockedIdentifierType;

public interface BlockedUserIdentifierRepository extends JpaRepository<BlockedUserIdentifier, Long> {

	boolean existsByIdentifierTypeAndIdentifierHash(BlockedIdentifierType identifierType, String identifierHash);

	List<BlockedUserIdentifier> findAllByUserId(String userId);
}
