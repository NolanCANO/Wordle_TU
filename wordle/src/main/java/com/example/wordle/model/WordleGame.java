package com.example.wordle.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WordleGame {

    private String targetWord;
    private int remainingAttempts = 6;
    private boolean isGameOver = false;
    private boolean isWon = false;
    private List<String> guesses = new ArrayList<>();
    private int wordLength = 5;
    private int mode = 1; // Mode de jeu (1=standard, 2=chronométré, 3=pratique, etc.) 
    private int timeLimitSeconds = 0; // Limite de temps en secondes
    private long startTimeMillis = System.currentTimeMillis();

    //Indique si le temps est écoulé (mode chrono)
    public boolean isTimeUp() {
        if (timeLimitSeconds <= 0) {
            return false; 
        }
        return getElapsedTimeSeconds() >= timeLimitSeconds;
    }

    //Renvoie le temps écoulé depuis le début de la partie (en secondes)
    public long getElapsedTimeSeconds() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000;
    }
}
