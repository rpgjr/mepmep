package com.mepmep.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Typed view of the "app" block in application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Tmdb tmdb, Jwt jwt, List<String> corsOrigins, Auth auth) {
    public record Tmdb(String baseUrl, String token) {}

    public record Jwt(String secret, int expiryHours) {}

    /** checkMx: look up the email domain in DNS before accepting a sign-up. */
    public record Auth(boolean checkMx) {}
}
