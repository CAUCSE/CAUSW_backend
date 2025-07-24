package net.causw.app.main.infrastructure.security.userdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.UserState;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final User user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoles().stream()
			.map(role -> new SimpleGrantedAuthority(role.authority()))
			.collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getName();
	}

	public String getUserId() {
		return user.getId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.getState() == UserState.ACTIVE;
	}

	public User getUser() {
		return this.user;
	}

	public UserState getUserState() {
		return user.getState();
	}
}
