package com.mepmep.library;

import jakarta.persistence.*;

import java.time.Instant;

/** One title a user saved to a list: FAVORITE or WATCHLIST. */
@Entity
@Table(name = "saved_titles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "listType", "mediaType", "tmdbId"}))
public class SavedTitle {
    public enum ListType { FAVORITE, WATCHLIST }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ListType listType;

    @Column(nullable = false, length = 5)
    private String mediaType;

    @Column(nullable = false)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    private String posterPath;
    private Double voteAverage;
    private String releaseYear;

    @Column(nullable = false)
    private Instant addedAt = Instant.now();

    protected SavedTitle() {}

    public SavedTitle(Long userId, ListType listType, String mediaType, Long tmdbId,
                      String title, String posterPath, Double voteAverage, String releaseYear) {
        this.userId = userId;
        this.listType = listType;
        this.mediaType = mediaType;
        this.tmdbId = tmdbId;
        this.title = title;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.releaseYear = releaseYear;
    }

    public Long getId() { return id; }
    public ListType getListType() { return listType; }
    public String getMediaType() { return mediaType; }
    public Long getTmdbId() { return tmdbId; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public Double getVoteAverage() { return voteAverage; }
    public String getReleaseYear() { return releaseYear; }
    public Instant getAddedAt() { return addedAt; }
}
