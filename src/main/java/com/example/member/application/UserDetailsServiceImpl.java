package com.example.member.application;

import com.example.member.dao.UserDao;
import com.example.member.domain.RoleEnum;
import com.example.member.domain.User;
import com.example.member.dto.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDao userDao;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userDao.findByEmail(username);
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException();
        }
        User user = optionalUser.get();
        return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPassword(), toAuthorities(user.getRole()));
    }

    private List<SimpleGrantedAuthority> toAuthorities(RoleEnum role) {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}
