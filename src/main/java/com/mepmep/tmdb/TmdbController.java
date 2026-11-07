package com.mepmep.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/** Public read-only endpoints that wrap TMDB. */
@RestController
@RequestMapping("/api")
public class TmdbController {
    private static final Set<String> TYPES = Set.of("movie", "tv");
    private static final Set<String> CATEGORIES = Set.of("popular", "top_rated", "now_playing", "on_the_air", "upcoming");

    private final TmdbService tmdb;

    public TmdbController(TmdbService tmdb) {
        this.tmdb = tmdb;
    }

    /** Trending titles of the week. */
    @GetMapping("/trending/{type}")
    public JsonNode trending(@PathVariable String type) {
        return tmdb.get("/trending/" + type(type) + "/week", b -> b);
    }

    /** Lists such as popular or top_rated. */
    @GetMapping("/list/{type}/{category}")
    public JsonNode list(@PathVariable String type, @PathVariable String category,
                         @RequestParam(defaultValue = "1") int page) {
        if (!CATEGORIES.contains(category)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad category");
        return tmdb.get("/" + type(type) + "/" + category, b -> b.queryParam("page", page));
    }

    /** Browse with an optional genre filter and sort. */
    @GetMapping("/discover/{type}")
    public JsonNode discover(@PathVariable String type,
                             @RequestParam(required = false) String genre,
                             @RequestParam(defaultValue = "popularity.desc") String sort,
                             @RequestParam(defaultValue = "1") int page) {
        return tmdb.get("/discover/" + type(type), b -> {
            b.queryParam("sort_by", sort).queryParam("page", page).queryParam("vote_count.gte", 50);
            return genre == null || genre.isBlank() ? b : b.queryParam("with_genres", genre);
        });
    }

    @GetMapping("/genres/{type}")
    public JsonNode genres(@PathVariable String type) {
        return tmdb.get("/genre/" + type(type) + "/list", b -> b);
    }

    @GetMapping("/search")
    public JsonNode search(@RequestParam String q, @RequestParam(defaultValue = "1") int page) {
        return tmdb.get("/search/multi", b -> b.queryParam("query", q).queryParam("page", page));
    }

    /** Full details, cast, similar titles and trailers in one call. */
    @GetMapping("/title/{type}/{id}")
    public JsonNode title(@PathVariable String type, @PathVariable long id) {
        return tmdb.get("/" + type(type) + "/" + id,
            b -> b.queryParam("append_to_response", "credits,similar,videos"));
    }

    /** Episode list of one season. */
    @GetMapping("/tv/{id}/season/{season}")
    public JsonNode season(@PathVariable long id, @PathVariable int season) {
        return tmdb.get("/tv/" + id + "/season/" + season, b -> b);
    }

    private String type(String t) {
        if (!TYPES.contains(t)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type must be movie or tv");
        return t;
    }
}
