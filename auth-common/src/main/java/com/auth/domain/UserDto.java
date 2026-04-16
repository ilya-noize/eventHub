package com.auth.domain;

import com.auth.api.UserResponse;
import com.auth.db.UserEntity;
import com.event.common.UserRole;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class UserDto implements UserDetails {
    private Long id;
    private String login;
    private String password;
    private Integer age;
    private List<SimpleGrantedAuthority> roles;

    public UserDto(Claims claims) {
        this.id = Long.valueOf(claims.getSubject());
        this.login = claims.get("login").toString();
        this.age = claims.get("age", Integer.class);

        Object roleClaim = claims.get("role");
        this.roles = new ArrayList<>();

        if (roleClaim instanceof List<?> roleList) {
            for (Object o : roleList) {
                if (o instanceof String str) {
                    // Если роль — строка: "ROLE_USER"
                    this.roles.add(new SimpleGrantedAuthority(str));
                } else if (o instanceof Map<?, ?> map && map.containsKey("authority")) {
                    // Если роль — объект: {"authority": "ROLE_ADMIN"}
                    Object authority = map.get("authority");
                    if (authority instanceof String authStr) {
                        this.roles.add(new SimpleGrantedAuthority(authStr));
                    }
                }
            }
        }
    }

    public String getSubject() {
        return String.valueOf(getId());
    }

    public Map<String, Object> getClaims() {
        return Map.of(
                "login", getLogin(),
                "age", getAge(),
                "role", getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }

    UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .login(login)
                .password(password)
                .age(age)
                .role(getRoleAsString(roles))
                .build();
    }

    public UserResponse toResponse() {
        return UserResponse.builder()
                .id(id)
                .login(login)
                .age(age)
                .role(getRoleAsString(roles))
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    private String getRoleAsString(List<SimpleGrantedAuthority> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserRole.USER.name();
        }
        return roles.getFirst().getAuthority();
    }
}
