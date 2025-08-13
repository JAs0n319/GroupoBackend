package me.jas0n.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;
import java.util.UUID;

public final class CurrentUser {
    private CurrentUser() {
    }

    /**
     * 取不到返回 null，不抛错
     */
    public static UUID idOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        // A) 标准：JwtAuthenticationToken
        if (auth instanceof JwtAuthenticationToken jat) {
            Object uid = jat.getTokenAttributes().get("uid");        // 先从 attributes
            if (uid != null) return parseUuid(uid);
            Jwt jwt = jat.getToken();                                // 再从 claims
            if (jwt != null) return parseUuid(jwt.getClaim("uid"));
        }

        // B) principal 就是 Jwt
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return parseUuid(jwt.getClaim("uid"));
        }

        // C) 某些自定义转换器：principal 是 Map
        if (principal instanceof Map<?, ?> map) {
            return parseUuid(map.get("uid"));
        }

        return null;
    }

    /**
     * 取不到直接抛 401（推荐在受保护的业务里用）
     */
    public static UUID idOrThrow() {
        UUID id = idOrNull();
        if (id == null) {
            throw new me.jas0n.common.error.BusinessException(
                    "UNAUTHORIZED", "未登录或凭证无效",
                    org.springframework.http.HttpStatus.UNAUTHORIZED
            );
        }
        return id;
    }

    private static UUID parseUuid(Object v) {
        if (v == null) return null;
        try {
            return UUID.fromString(v.toString());
        } catch (Exception ignore) {
            return null;
        }
    }
}