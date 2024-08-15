package net.causw.config.security;

import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.model.enums.UserState;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    public boolean isActiveAndNotNoneUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserState() == UserState.ACTIVE &&
                userDetails.getAuthorities().stream()
                        .noneMatch(authority -> authority.getAuthority().equals("ROLE_NONE"));
    }
}
