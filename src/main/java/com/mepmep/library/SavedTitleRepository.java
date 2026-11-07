package com.mepmep.library;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SavedTitleRepository extends JpaRepository<SavedTitle, Long> {
    List<SavedTitle> findByUserIdAndListTypeOrderByAddedAtDesc(Long userId, SavedTitle.ListType type);

    List<SavedTitle> findByUserIdAndMediaTypeAndTmdbId(Long userId, String mediaType, Long tmdbId);

    boolean existsByUserIdAndListTypeAndMediaTypeAndTmdbId(Long userId, SavedTitle.ListType type, String mediaType, Long tmdbId);

    @Transactional
    long deleteByUserIdAndListTypeAndMediaTypeAndTmdbId(Long userId, SavedTitle.ListType type, String mediaType, Long tmdbId);
}
