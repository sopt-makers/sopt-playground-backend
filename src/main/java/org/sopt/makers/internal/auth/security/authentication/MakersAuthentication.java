package org.sopt.makers.internal.auth.security.authentication;


import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class MakersAuthentication implements Authentication {

    private final Long userId;
    private final List<GrantedAuthority> authorities;
    private boolean authenticated = false;

    public MakersAuthentication(String userId, List<String> roles) {
        this.userId = Long.valueOf(userId);
        this.authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();
    }

    public List<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null; // JWT 기반 인증이라 자격 증명 X
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
