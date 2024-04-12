package com.psear.member.dao;

import com.psear.member.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserDao extends JpaRepository<User, Long> {
    @Override
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    Optional<User> findById(@NonNull Long id);

    Optional<User> findByEmail(String email);
}
