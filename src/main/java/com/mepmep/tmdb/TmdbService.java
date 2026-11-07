package com.mepmep.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.mepmep.config.AppProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Calls TMDB with the secret token. The browser never sees the token. */
@Service
public class TmdbService {
    private static final long TTL_MS = 5 * 60 * 1000;

    private record Cached(long at, JsonNode body) {}

    private final RestClient client;
    private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

    public TmdbService(AppProperties props) {
        this.client = RestClient.builder()
            .baseUrl(props.tmdb().baseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.tmdb().token())
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .build();
    }

    /** GET a TMDB path. Results are kept 5 minutes so repeat visits are fast. */
    public JsonNode get(String path, Function<UriBuilder, UriBuilder> params) {
        String key = path + "|" + params.apply(new org.springframework.web.util.DefaultUriBuilderFactory().builder()).build();
        Cached hit = cache.get(key);
        if (hit != null && System.currentTimeMillis() - hit.at < TTL_MS) {
            return hit.body;
        }
        JsonNode body = client.get()
            .uri(b -> params.apply(b.path(path).queryParam("language", "en-US")).build())
            .retrieve()
            .body(JsonNode.class);
        cache.put(key, new Cached(System.currentTimeMillis(), body));
        return body;
    }
}
