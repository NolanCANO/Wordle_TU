package com.example.wordle.service;

import com.example.wordle.model.GameStats;
import com.example.wordle.model.WordleGame;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class WordleService {

    private final List<String> fullDictionary; // Dictionnaire complet
    private final GameStats stats = new GameStats(); // Statistiques du joueur

    public WordleService() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("listeMots.txt").getInputStream()))
        ) {
            this.fullDictionary = reader.lines()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger les mots", e);
        }
    }

    public WordleGame startNewGame(int length, int modeChoice) {
        // Filtrer le dictionnaire pour obtenir uniquement des mots de la longueur souhaitée
        List<String> filteredDict = fullDictionary.stream()
                .filter(w -> w.length() == length)
                .collect(Collectors.toList());

        if (filteredDict.isEmpty()) {
            throw new IllegalArgumentException("Aucun mot de longueur " + length + " trouvé.");
        }

        // Sélection aléatoire d'un mot
        String randomWord = filteredDict.get(new Random().nextInt(filteredDict.size()));

        WordleGame game = new WordleGame();
        game.setTargetWord(randomWord);
        game.setWordLength(length);
        game.setMode(modeChoice);

        // Configuration des essais / chrono selon le mode
        switch (modeChoice) {
            case 2: // Chronométré
                game.setRemainingAttempts(6);
                game.setTimeLimitSeconds(60);
                break;
            case 3: // Pratique
                game.setRemainingAttempts(10);
                break;
            case 1: // Standard
            default:
                game.setRemainingAttempts(6);
                break;
        }
        return game;
    }

    //Version basique (5 lettres, mode standard).
    public WordleGame startNewGame() {
        return startNewGame(5, 1);
    }

    //Valide et analyse le mot proposé par le joueur.
    public String checkGuess(WordleGame game, String guess) {
        int length = game.getWordLength();

        // Validation de la saisie
        if (guess.length() != length || !guess.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+")) {
            throw new IllegalArgumentException("Mot invalide (doit faire " + length + " lettres).");
        }

        // Mode chrono : temps écoulé ?
        if (game.isTimeUp()) {
            game.setGameOver(true);
            throw new IllegalStateException("Temps écoulé !");
        }

        guess = guess.toUpperCase();
        String target = game.getTargetWord();

        // Fréquences de chaque lettre du mot cible
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : target.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        // Tableau de résultat (par ex. "[X]", "[O]", "[_]")
        String[] resultArray = new String[length];

        // 1er passage : repérer les [X] (lettres exactes)
        for (int i = 0; i < length; i++) {
            char g = guess.charAt(i);
            char t = target.charAt(i);
            if (g == t) {
                resultArray[i] = "[X]";
                freq.put(g, freq.get(g) - 1);
            }
        }

        // 2e passage : repérer les [O] (lettres présentes ailleurs) ou [_] (absentes)
        for (int i = 0; i < length; i++) {
            if (resultArray[i] == null) {
                char g = guess.charAt(i);
                if (freq.containsKey(g) && freq.get(g) > 0) {
                    resultArray[i] = "[O]";
                    freq.put(g, freq.get(g) - 1);
                } else {
                    resultArray[i] = "[_]";
                }
            }
        }

        // Mise à jour des tentatives
        game.setRemainingAttempts(game.getRemainingAttempts() - 1);
        game.getGuesses().add(guess);

        // Vérification si toutes les lettres sont correctes
        boolean allCorrect = Arrays.stream(resultArray).allMatch("[X]"::equals);

        if (allCorrect) {
            // Partie gagnée
            game.setWon(true);
            game.setGameOver(true);
            updateScore(game);
            stats.updateFromGame(game);
        } else if (game.getRemainingAttempts() == 0) {
            // Partie perdue
            game.setGameOver(true);
            updateScore(game);
            stats.updateFromGame(game);
        }

        return String.join("", resultArray);
    }

    public void updateScore(WordleGame game) {
        int baseScore = 1000;
        int usedAttempts = game.getGuesses().size();
        int attemptsPenalty = (usedAttempts - 1) * 100;
        int score = baseScore - attemptsPenalty;

        if (game.getMode() == 2 && game.isWon()) {
            long timeLeft = game.getTimeLimitSeconds() - game.getElapsedTimeSeconds();
            if (timeLeft > 0) {
                score += timeLeft * 5;
            }
        }

        game.setScore(Math.max(score, 0));
    }

    public GameStats getStats() {
        return stats;
    }

    public int getMinWordLength() {
        if (fullDictionary.isEmpty()) {
            throw new IllegalStateException("Le dictionnaire est vide.");
        }
        // On trouve la longueur minimale
        return fullDictionary.stream()
                             .mapToInt(String::length)
                             .min()
                             .orElseThrow(() -> new IllegalStateException("Aucun mot trouvé."));
    }
    
    public int getMaxWordLength() {
        if (fullDictionary.isEmpty()) {
            throw new IllegalStateException("Le dictionnaire est vide.");
        }
        // On trouve la longueur maximale
        return fullDictionary.stream()
                             .mapToInt(String::length)
                             .max()
                             .orElseThrow(() -> new IllegalStateException("Aucun mot trouvé."));
    }
    
}
