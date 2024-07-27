package net.causw.config.security.userdetails;

import lombok.AllArgsConstructor;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return CustomUserDetails.builder()
                .user(user)
                .build();
    }

    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                        )
                );

        return CustomUserDetails.builder()
                .user(user)
                .build();
    }
}

