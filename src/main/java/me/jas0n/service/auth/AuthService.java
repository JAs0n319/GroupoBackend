package me.jas0n.service.auth;

import me.jas0n.common.error.BusinessException;
import me.jas0n.domain.user.User;
import me.jas0n.repository.UserRepository;
import me.jas0n.web.auth.dto.AuthTokens;
import me.jas0n.web.auth.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokenService) {
        this.users = users;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public User register(me.jas0n.web.auth.dto.RegisterRequest req) {
        String email = req.email().toLowerCase().trim();

        if (users.existsByEmail(email)) {
            throw BusinessException.bad("EMAIL_IN_USE", "该邮箱已被注册");
        }

        if (!req.password().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw BusinessException.bad("WEAK_PASSWORD", "密码至少8位，且包含字母和数字");
        }

        User u = new User();
        u.setEmail(email);
        u.setName(req.name().trim());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setEmailVerified(false);

        return users.save(u);
    }

    public AuthTokens login(LoginRequest req) {
        String email = req.email().toLowerCase().trim();
        User user = users.findByEmail(email)
                .orElseThrow(() -> BusinessException.bad("INVALID_CREDENTIALS", "邮箱或密码错误"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw BusinessException.bad("INVALID_CREDENTIALS", "邮箱或密码错误");
        }

        Map<String, Object> accessClaims = Map.of(
                "uid", user.getId().toString(),
                "typ", "access"
        );

        String access = tokenService.generateAccess(email, accessClaims);
        String refresh = tokenService.generateRefresh(email);

        return new AuthTokens(access, refresh);
    }

    public AuthTokens refresh(String refreshToken) {
        try {
            var jws = tokenService.parse(refreshToken);
            var claims = jws.getBody();

            if (!"refresh".equals(claims.get("typ"))) {
                throw new BusinessException("INVALID_TOKEN", "无效的刷新令牌", HttpStatus.UNAUTHORIZED);
            }

            String email = claims.getSubject();
            var user = users.findByEmail(email.toLowerCase())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在", HttpStatus.UNAUTHORIZED));

            Map<String, Object> accessClaims = Map.of(
                    "uid", user.getId().toString(),
                    "typ", "access"
            );
            String newAccess = tokenService.generateAccess(email, accessClaims);
            String newRefresh = tokenService.generateRefresh(email);

            return new AuthTokens(newAccess, newRefresh);

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            throw new BusinessException("TOKEN_EXPIRED", "刷新令牌已过期", HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            throw new BusinessException("INVALID_TOKEN", "无效的刷新令牌", HttpStatus.UNAUTHORIZED);
        }
    }
}