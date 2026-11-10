package com.project.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Кто смотрит: гость (userId == null), пользователь или администратор.
 */
public record Viewer(Long userId, boolean admin, String bearerToken) {

    public static Viewer from(Jwt jwt) {
        if (jwt == null) {
            return new Viewer(null, false, null);
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        boolean admin = roles != null && (roles.contains("Admin") || roles.contains("Develop_admin"));
        return new Viewer(Long.valueOf(jwt.getSubject()), admin, jwt.getTokenValue());
    }

    public boolean isAnonymous() {
        return userId == null;
    }

    public boolean canManage(Long authorId) {
        return admin || (userId != null && userId.equals(authorId));
    }
}
