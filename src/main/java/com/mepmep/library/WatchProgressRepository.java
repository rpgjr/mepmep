package com.mepmep.library;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {
    List<WatchProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<WatchProgress> findByUserIdAndMediaTypeAndTmdbId(Long userId, String mediaType, Long tmdbId);

    @Transactional
    long deleteByUserIdAndMediaTypeAndTmdbId(Long userId, String mediaType, Long tmdbId);
}
