package com.example.wordle.model;

import lombok.Getter;

@Getter
public class GameStats {

    private int totalGames = 0;
    private int wins = 0;
    private int currentStreak = 0;
    private int bestStreak = 0;
    private int totalAttempts = 0;

    // Met à jour les statistiques après une partie.
    public void updateFromGame(WordleGame game) {
        totalGames++;
        totalAttempts += game.getGuesses().size();

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
}
