package com.psear.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Convert(converter = RoleEnumConverter.class)
    private RoleEnum role = RoleEnum.ROLE_USER;

    @Builder
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}