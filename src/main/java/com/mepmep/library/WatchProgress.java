package com.mepmep.library;

import jakarta.persistence.*;

import java.time.Instant;

/** Where a user stopped watching. One row per user and title (latest episode for series). */
@Entity
@Table(name = "watch_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "mediaType", "tmdbId"}))
public class WatchProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 5)
    private String mediaType;

    @Column(nullable = false)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    private String posterPath;
    private String backdropPath;

    @Column(nullable = false)
    private int season = 1;

    @Column(nullable = false)
    private int episode = 1;

    @Column(nullable = false)
    private int positionSec;

    @Column(nullable = false)
    private int durationSec;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected WatchProgress() {}

    public WatchProgress(Long userId, String mediaType, Long tmdbId) {
        this.userId = userId;
        this.mediaType = mediaType;
        this.tmdbId = tmdbId;
    }

    public void update(String title, String posterPath, String backdropPath,
                       int season, int episode, int positionSec, int durationSec) {
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.season = season;
        this.episode = episode;
        this.positionSec = positionSec;
        this.durationSec = durationSec;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getMediaType() { return mediaType; }
    public Long getTmdbId() { return tmdbId; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public int getSeason() { return season; }
    public int getEpisode() { return episode; }
    public int getPositionSec() { return positionSec; }
    public int getDurationSec() { return durationSec; }
    public Instant getUpdatedAt() { return updatedAt; }
}
