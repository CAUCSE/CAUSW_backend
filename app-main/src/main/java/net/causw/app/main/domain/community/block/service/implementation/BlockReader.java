package net.causw.app.main.domain.community.block.service.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.relation.userBlock.UserBlockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockReader {

	private final UserBlockRepository userBlockRepository;

	public boolean existsByBlockerAndBlocked(User blocker, User blocked) {
		return userBlockRepository.existsByBlockerIdAndBlockeeId(blocker.getId(), blocked.getId());
	}

	public List<String> findBlockedUserIds(String blockerId) {
		return new ArrayList<>(userBlockRepository.findBlockeeIdsByBlockerUserId(blockerId));
	}
}
