package com.project.testApp.service;

import com.project.testApp.dto.response.TokenResponseDto;
import com.project.testApp.entity.Person;
import com.project.testApp.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Выдаёт пару access + refresh; refresh нельзя использовать как access и наоборот (claim token_type)
 */
@Service
public class TokenService {
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final PersonRepository personRepository;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    /**
     * Свой декодер: общий бин принимает только access-токены.
     */
    public TokenService(JwtEncoder jwtEncoder, SecretKey jwtSecretKey, PersonRepository personRepository,
                        @Value("${app.jwt.access-ttl}") Duration accessTtl,
                        @Value("${app.jwt.refresh-ttl}") Duration refreshTtl) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
        this.personRepository = personRepository;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public TokenResponseDto issueTokens(Person person) {
        return new TokenResponseDto(
                encode(person, ACCESS, accessTtl),
                encode(person, REFRESH, refreshTtl),
                accessTtl.toSeconds());
    }

    /**
     * Берём пользователя заново: роль могла измениться, а аккаунт - удалиться.
     */
    public TokenResponseDto refresh(String refreshToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new BadCredentialsException("Refresh-токен недействителен или истёк");
        }
        if (!REFRESH.equals(jwt.getClaimAsString("token_type"))) {
            throw new BadCredentialsException("Передан не refresh-токен");
        }
        Person person = personRepository.findById(Long.valueOf(jwt.getSubject()))
                .orElseThrow(() -> new BadCredentialsException("Пользователь больше не существует"));
        return issueTokens(person);
    }

    private String encode(Person person, String tokenType, Duration ttl) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nowhow-user-profile")
                .subject(String.valueOf(person.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("username", person.getUserName())
                .claim("roles", List.of(person.getPersonRoles().name()))
                .claim("token_type", tokenType)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
