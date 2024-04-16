package com.pser.member.dto;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class UserDetailsImpl extends User {
    private final Long id;

    public UserDetailsImpl(Long id, String username, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}
