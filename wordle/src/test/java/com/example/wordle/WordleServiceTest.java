package com.example.wordle;

import com.example.wordle.model.WordleGame;
import com.example.wordle.service.WordleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordleServiceTest {

    private WordleService service;

    @BeforeEach
    void setUp() {
        service = new WordleService();
    }

    @Test
    void startNewGame_standardMode() {
        WordleGame game = service.startNewGame();
        assertNotNull(game.getTargetWord(), "Le mot cible ne doit pas être null");
        assertEquals(5, game.getWordLength(), "La longueur du mot doit être par défaut 5");
        assertEquals(6, game.getRemainingAttempts(), "Les essais restants doivent être initialisés à 6");
        assertEquals(1, game.getMode(), "Le mode par défaut doit être 1 (standard)");
    }

    @Test
    void startNewGame_customMode() {
        WordleGame game = service.startNewGame(7, 3);
        assertEquals(7, game.getWordLength(), "La longueur du mot doit être personnalisée à 7");
        assertEquals(10, game.getRemainingAttempts(), "Les essais restants en mode pratique doivent être 10");
        assertEquals(3, game.getMode(), "Le mode doit être 3 (pratique)");
    }

    @Test
    void checkGuess_exactMatch() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);

        String feedback = service.checkGuess(game, "APPLE");

        assertEquals("[X][X][X][X][X]", feedback);
        assertTrue(game.isWon());
        assertTrue(game.isGameOver());
    }

    @Test
    void checkGuess_partialMatch() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);

        String feedback = service.checkGuess(game, "ALERT");

        assertEquals("[X][O][O][_][_]", feedback);
        assertFalse(game.isWon());
        assertFalse(game.isGameOver());
        assertEquals(5, game.getRemainingAttempts());
    }

    @Test
    void checkGuess_invalidLength() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.checkGuess(game, "APP");
        });

        assertEquals("Mot invalide (doit faire 5 lettres).", exception.getMessage());
    }

    @Test
    void checkGuess_noAttemptsLeft() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);
        game.setRemainingAttempts(1);

        String feedback = service.checkGuess(game, "ALERT");

        assertFalse(game.isWon());
        assertTrue(game.isGameOver());
        assertEquals(0, game.getRemainingAttempts());
    }

    @Test
    void checkGuess_timeUp() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);
        game.setTimeLimitSeconds(1);
        game.setStartTimeMillis(System.currentTimeMillis() - 2000); // simule 2 secondes écoulées

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            service.checkGuess(game, "ALERT");
        });

        assertEquals("Temps écoulé !", exception.getMessage());
        assertTrue(game.isGameOver());
    }

    @Test
    void checkGuess_duplicateLetters() {
        WordleGame game = new WordleGame();
        game.setTargetWord("LEVEL");
        game.setWordLength(5);

        String feedback = service.checkGuess(game, "HELLO");

        assertEquals("[_][X][O][O][_]", feedback);
        assertFalse(game.isWon());
        assertFalse(game.isGameOver());
    }
}
