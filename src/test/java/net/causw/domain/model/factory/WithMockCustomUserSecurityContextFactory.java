package net.causw.domain.model.factory;

import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.aop.annotation.WithMockCustomUser;
import net.causw.domain.model.util.ObjectFixtures;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Set;

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
