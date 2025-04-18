package com.example.wordle.repository;

import com.example.wordle.model.GameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameStatsRepository extends JpaRepository<GameStats, Long> {
}
