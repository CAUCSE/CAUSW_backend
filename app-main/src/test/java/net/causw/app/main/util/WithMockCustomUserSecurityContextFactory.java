package net.causw.app.main.util;

import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		User mockUser = ObjectFixtures.getUser();
		mockUser.setRoles(Set.of(annotation.roles()));
		mockUser.setState(annotation.state());
		mockUser.setAcademicStatus(annotation.academicStatus());

		CustomUserDetails userDetails = new CustomUserDetails(mockUser);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		context.setAuthentication(authentication);

		return context;
	}
}
