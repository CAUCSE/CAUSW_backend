package net.causw.app.main.domain.community.block.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;
import net.causw.app.main.domain.user.relation.relation.userBlock.UserBlockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class BlockWriter {

	private final UserBlockRepository userBlockRepository;

	public UserBlock save(UserBlock userBlock) {
		return userBlockRepository.save(userBlock);
	}
}
