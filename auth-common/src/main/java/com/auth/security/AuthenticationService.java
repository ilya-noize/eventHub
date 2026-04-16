package com.auth.security;

import com.auth.api.JwtResponse;
import com.auth.api.UserCredentials;
import com.auth.db.RefreshToken;
import com.auth.db.UserEntity;
import com.auth.domain.RefreshTokenService;
import com.auth.domain.UserDto;
import com.auth.domain.UserMapper;
import com.auth.domain.UserService;
import com.event.common.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final AuthorizationService authorizationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final UserService userService;
    private final UserMapper userMapper;

    public JwtResponse authenticateUser(UserCredentials credentials) {
        var authentication = new UsernamePasswordAuthenticationToken(
                credentials.login(),
                credentials.password()
        );
        authenticationManager.authenticate(authentication);
        UserEntity user = userService.findByUsername(credentials.login());
        String accessToken = jwtTokenGenerator.generateToken(userMapper.toDomain(user));
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh accessToken"));

        UserDto userDto = userService.getUserById(token.getUser().getId());

        String newAccessToken = jwtTokenGenerator.generateToken(userDto);
        String newRefreshToken = refreshTokenService.createRefreshToken(token.getUser()).getToken();

        return new JwtResponse(newAccessToken, newRefreshToken);
    }

    public void logout() {
        Long userId = authorizationService.getCurrentAuthorizedUserId();
        refreshTokenService.deleteByUserId(userId);
    }

    @Deprecated
    public void verifyAuthenticatedUserAsOwnerResourceOrAdmin(Long ownerId) {
        UserDto authUserDto = authorizationService.getCurrentAuthorizedUser();
        if (!ownerId.equals(authUserDto.getId())) {
            var adminAuthority = new SimpleGrantedAuthority(UserRole.ADMIN.name());
            if (!authUserDto.getAuthorities().contains(adminAuthority)) {
                throw new SecurityException("The owner of the resource is another user");
            }
        }
    }
}
