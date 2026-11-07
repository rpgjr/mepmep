package com.mepmep.library;

import com.mepmep.library.SavedTitle.ListType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Favorites, watch list and continue-watching. Every endpoint needs a login. */
@RestController
@RequestMapping("/api")
public class LibraryController {
    public record SaveRequest(@NotBlank @Pattern(regexp = "movie|tv") String mediaType,
                              @NotNull Long tmdbId,
                              @NotBlank String title,
                              String posterPath,
                              Double voteAverage,
                              String releaseYear) {}

    public record ProgressRequest(@NotBlank @Pattern(regexp = "movie|tv") String mediaType,
                                  @NotNull Long tmdbId,
                                  @NotBlank String title,
                                  String posterPath,
                                  String backdropPath,
                                  @Min(1) int season,
                                  @Min(1) int episode,
                                  @Min(0) int positionSec,
                                  @Min(0) int durationSec) {}

    private final SavedTitleRepository saved;
    private final WatchProgressRepository progress;

    public LibraryController(SavedTitleRepository saved, WatchProgressRepository progress) {
        this.saved = saved;
        this.progress = progress;
    }

    // ---- favorites / watchlist ----

    @GetMapping("/library/{list}")
    public List<SavedTitle> list(@PathVariable String list, Authentication auth) {
        return saved.findByUserIdAndListTypeOrderByAddedAtDesc(uid(auth), listType(list));
    }

    @PostMapping("/library/{list}")
    @ResponseStatus(HttpStatus.CREATED)
    public SavedTitle add(@PathVariable String list, @Valid @RequestBody SaveRequest req, Authentication auth) {
        Long uid = uid(auth);
        ListType type = listType(list);
        if (saved.existsByUserIdAndListTypeAndMediaTypeAndTmdbId(uid, type, req.mediaType(), req.tmdbId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already in list");
        }
        return saved.save(new SavedTitle(uid, type, req.mediaType(), req.tmdbId(),
            req.title(), req.posterPath(), req.voteAverage(), req.releaseYear()));
    }

    @DeleteMapping("/library/{list}/{mediaType}/{tmdbId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String list, @PathVariable String mediaType,
                       @PathVariable Long tmdbId, Authentication auth) {
        saved.deleteByUserIdAndListTypeAndMediaTypeAndTmdbId(uid(auth), listType(list), mediaType, tmdbId);
    }

    /** Is this title already in my favorites / watch list? Used to paint the buttons. */
    @GetMapping("/library/status/{mediaType}/{tmdbId}")
    public Map<String, Boolean> status(@PathVariable String mediaType, @PathVariable Long tmdbId, Authentication auth) {
        var rows = saved.findByUserIdAndMediaTypeAndTmdbId(uid(auth), mediaType, tmdbId);
        return Map.of(
            "favorite", rows.stream().anyMatch(r -> r.getListType() == ListType.FAVORITE),
            "watchlist", rows.stream().anyMatch(r -> r.getListType() == ListType.WATCHLIST));
    }

    // ---- continue watching ----

    @GetMapping("/history")
    public List<WatchProgress> history(Authentication auth) {
        return progress.findByUserIdOrderByUpdatedAtDesc(uid(auth));
    }

    @GetMapping("/history/{mediaType}/{tmdbId}")
    public WatchProgress historyOne(@PathVariable String mediaType, @PathVariable Long tmdbId, Authentication auth) {
        return progress.findByUserIdAndMediaTypeAndTmdbId(uid(auth), mediaType, tmdbId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No history"));
    }

    /** Create or update the resume point. */
    @PutMapping("/history")
    public WatchProgress saveProgress(@Valid @RequestBody ProgressRequest r, Authentication auth) {
        Long uid = uid(auth);
        WatchProgress row = progress.findByUserIdAndMediaTypeAndTmdbId(uid, r.mediaType(), r.tmdbId())
            .orElseGet(() -> new WatchProgress(uid, r.mediaType(), r.tmdbId()));
        row.update(r.title(), r.posterPath(), r.backdropPath(), r.season(), r.episode(), r.positionSec(), r.durationSec());
        return progress.save(row);
    }

    @DeleteMapping("/history/{mediaType}/{tmdbId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProgress(@PathVariable String mediaType, @PathVariable Long tmdbId, Authentication auth) {
        progress.deleteByUserIdAndMediaTypeAndTmdbId(uid(auth), mediaType, tmdbId);
    }

    private Long uid(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    private ListType listType(String path) {
        return switch (path) {
            case "favorites" -> ListType.FAVORITE;
            case "watchlist" -> ListType.WATCHLIST;
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown list");
        };
    }
}
