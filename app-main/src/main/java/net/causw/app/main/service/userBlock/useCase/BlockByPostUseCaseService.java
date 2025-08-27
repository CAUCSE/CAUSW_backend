package net.causw.app.main.service.userBlock.useCase;

import org.springframework.stereotype.Service;

import net.causw.app.main.dto.userBlock.response.CreateBlockByPostResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;

@Service
public class BlockByPostUseCaseService {
	public CreateBlockByPostResponseDto execute(CustomUserDetails userDetails, String postId) {

		return null;
	}
}
