package com.example.wordle;

import com.example.wordle.model.WordleGame;
import com.example.wordle.service.WordleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

class WordleServiceTest {

    private WordleService service;

    @BeforeEach
    void setUp() {
        service = new WordleService();
    }
    // Vérifie qu'une partie standard (5 lettres, mode=1) s'initialise correctement.
    @Test
    void startNewGame_standardMode() {
        WordleGame game = service.startNewGame();
        assertNotNull(game.getTargetWord(), "Le mot cible ne doit pas être null");
        assertEquals(5, game.getWordLength(), "La longueur du mot doit être par défaut 5");
        assertEquals(6, game.getRemainingAttempts(), "Les essais restants doivent être initialisés à 6");
        assertEquals(1, game.getMode(), "Le mode par défaut doit être 1 (standard)");
    }

    // Vérifie qu'une partie personnalisée (7 lettres, mode=3) s'initialise correctement.
    @Test
    void startNewGame_customMode() {
        WordleGame game = service.startNewGame(7, 3);
        assertEquals(7, game.getWordLength(), "La longueur du mot doit être personnalisée à 7");
        assertEquals(10, game.getRemainingAttempts(), "Les essais restants en mode pratique doivent être 10");
        assertEquals(3, game.getMode(), "Le mode doit être 3 (pratique)");
    }

    // Vérifie qu'un mode inconnu retombe sur la configuration standard (6 essais) tout en conservant la valeur "mode".
    @Test
    void startNewGame_unknownMode_shouldDefaultToStandard() {
        WordleGame game = service.startNewGame(5, 99);
        assertEquals(5, game.getWordLength());
        // Puisque le switch par défaut retombe sur le case standard
        assertEquals(6, game.getRemainingAttempts(), "Doit retomber sur 6 essais en mode standard");
        assertEquals(99, game.getMode(), "Le champ mode reste 99, mais la config s'applique comme standard.");
    }

    // Vérifie qu'une erreur est lancée si la longueur de mot n'existe pas dans le dictionnaire.
    @Test
    void startNewGame_lengthNotInDictionary_shouldThrow() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.startNewGame(30, 1);
        });
        assertTrue(exception.getMessage().contains("Aucun mot de longueur 30"));
    }
    
    // Vérifie qu'un essai exact (mot identique) donne 5 fois [X] et finit la partie.
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

    // Vérifie qu'un essai partiellement correct (quelques lettres présentes) retourne [X][O][O][_][_].
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

    // Vérifie qu'un mot invalide (pas la bonne taille) lance une exception.
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

    // Vérifie qu'une partie s'arrête quand les essais tombent à 0.
    @Test
    void checkGuess_noAttemptsLeft() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);
        game.setRemainingAttempts(1);

        String feedback = service.checkGuess(game, "ALERT");

        assertNotNull(feedback);
        assertFalse(feedback.isEmpty(), "Le feedback ne doit pas être vide");
        assertFalse(game.isWon());
        assertTrue(game.isGameOver());
        assertEquals(0, game.getRemainingAttempts());
    }

    // Vérifie qu'une exception est lancée si le temps est écoulé en mode chrono.
    @Test
    void checkGuess_timeUp() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);
        game.setTimeLimitSeconds(1);
        game.setStartTimeMillis(System.currentTimeMillis() - 2000); // simule 2s écoulées

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            service.checkGuess(game, "ALERT");
        });

        assertEquals("Temps écoulé !", exception.getMessage());
        assertTrue(game.isGameOver());
    }

    // Vérifie qu'un essai avec lettres en doublon renvoie le bon feedback.
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

    // Vérifie que lorqu'il y a plusieurs lettres doublons, il n'y ai que le nombre exacte de manquante qui soit en [O].
    @Test
    void checkGuess_duplicateLetters3() {
        WordleGame game = new WordleGame();
        game.setTargetWord("LEVEL");
        game.setWordLength(5);

        String feedback = service.checkGuess(game, "LELLO");

        assertEquals("[X][X][O][_][_]", feedback);
        assertFalse(game.isWon());
        assertFalse(game.isGameOver());
    }

    // Vérifie qu'une lettre déjà [X] n'est pas re-signalée [O] plus loin.
    @Test
    void checkGuess_consumedLetterShouldNotGiveO() {
        WordleGame game = new WordleGame();
        game.setTargetWord("BOOKS");
        game.setWordLength(5);

        String feedback = service.checkGuess(game, "BOOOB");

        assertEquals("[X][X][X][_][_]", feedback);
        assertFalse(game.isWon());
        assertFalse(game.isGameOver());
        assertEquals(5, game.getRemainingAttempts());
    }

    // Vérifie que getMinWordLength() renvoie une valeur > 0 (si le dico n'est pas vide).
    @Test
    void getMinWordLength_shouldReturnPositiveValue() {
        int min = service.getMinWordLength();
        assertTrue(min > 0, "La longueur minimale doit être > 0");
    }

    // Vérifie que getMaxWordLength() renvoie une valeur >= min (si le dico n'est pas vide).
    @Test
    void getMaxWordLength_shouldBeAtLeastMin() {
        int min = service.getMinWordLength();
        int max = service.getMaxWordLength();
        assertTrue(max >= min, "La longueur maximale doit être >= la longueur minimale");
    }

    // Teste le comportement getMinWordLength() de si le dictionnaire est vide.
    @Test
    void getMinWordLength_emptyDictionary_shouldThrow() throws Exception {
        makeDictionaryEmpty(service);
        assertThrows(IllegalStateException.class, () -> {
            service.getMinWordLength();
        });
    }

    // Teste le comportement getMaxWordLength() de si le dictionnaire est vide.
    @Test
    void getMaxWordLength_emptyDictionary_shouldThrow() throws Exception {
        makeDictionaryEmpty(service);
        assertThrows(IllegalStateException.class, () -> {
            service.getMaxWordLength();
        });
    }

    // Méthode pour rendre fullDictionary vide
    private void makeDictionaryEmpty(WordleService svc) throws Exception {
        // On récupère le champ private final List<String> fullDictionary
        Field dictField = WordleService.class.getDeclaredField("fullDictionary");
        dictField.setAccessible(true);
        // On force ce champ à une liste vide
        dictField.set(svc, new ArrayList<>());
    }
}
