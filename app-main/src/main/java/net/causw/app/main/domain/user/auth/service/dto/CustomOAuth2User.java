package net.causw.app.main.domain.user.auth.service.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import net.causw.app.main.domain.user.account.entity.user.User;

public record CustomOAuth2User(
	User user,
	Map<String, Object> attributes,
	String nameAttributeKey) implements OAuth2User {

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(user.getState().name()));
	}

	@Override
	public String getName() {
		return String.valueOf(attributes.get(nameAttributeKey));
	}

	public String getEmail() {
		return user.getEmail();
	}
}
