package org.sopt.makers.internal.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class InternalMemberDetails implements UserDetails {
    private Collection<? extends GrantedAuthority> authorities;
    private final String username;
    private final String authUserId;

    public InternalMemberDetails(Member member) {
        this.username = member.getName();
        this.authUserId = member.getAuthUserId();
        this.authorities = List.of(new SimpleGrantedAuthority("Member"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return authUserId;
    }

    @Override
    public String getUsername() {
        return username;
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
        return true;
    }
}
