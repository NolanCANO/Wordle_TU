package com.example.wordle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class GameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalGames = 0;
    private int wins = 0;
    private int currentStreak = 0;
    private int bestStreak = 0;
    private int totalAttempts = 0;
    private int totalScore = 0;

    // Met à jour les statistiques après une partie.
    public void updateFromGame(WordleGame game) {
        totalGames++;
        totalAttempts += game.getGuesses().size();
        totalScore += game.getScore();

        if (game.isWon()) {
            wins++;
            currentStreak++;
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }
        } else {
            currentStreak = 0;
        }
    }

    // Calcule la moyenne de tentatives par partie.
    public double getAverageAttempts() {
        if (totalGames == 0) return 0.0;
        return (double) totalAttempts / totalGames;
    }

    // Calcule la moyenne de score par partie.
    public double getAverageScore() {
        if (totalGames == 0) return 0.0;
        return (double) totalScore / totalGames;
    }
} 
