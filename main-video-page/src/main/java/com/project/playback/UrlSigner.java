package com.project.playback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Временные подписанные ссылки: тег &lt;video&gt;/&lt;img&gt; не умеет слать Authorization, поэтому доступ в самой ссылке.
 */
@Component
public class UrlSigner {

    private final SecretKeySpec key;

    /**
     * Свой префикс у ключа: подпись ссылки нельзя выдать за подпись JWT и наоборот.
     */
    public UrlSigner(@Value("${app.jwt.secret}") String secret) {
        this.key = new SecretKeySpec(("nowhow-url:" + secret).getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String sign(String purpose, Long id, long expiresAtEpochSeconds) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] signature = mac.doFinal((purpose + ":" + id + ":" + expiresAtEpochSeconds).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean verify(String purpose, Long id, Long expiresAtEpochSeconds, String signature) {
        if (expiresAtEpochSeconds == null || signature == null
                || expiresAtEpochSeconds < Instant.now().getEpochSecond()) {
            return false;
        }
        byte[] expected = sign(purpose, id, expiresAtEpochSeconds).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, signature.getBytes(StandardCharsets.UTF_8));
    }
}
