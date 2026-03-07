package net.causw.app.main.domain.user.account.api.v1.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFindIdResponseDto {
	String email;
}
