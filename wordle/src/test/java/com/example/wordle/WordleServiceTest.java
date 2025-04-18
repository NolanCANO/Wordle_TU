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

    // Vérifie qu'une erreur est lancée si la longueur de mot n'existe pas dans le dictionnaire.
    @Test
    void startNewGame_lengthNotInDictionary_shouldThrow() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.startNewGame(30, 1);
        });
        assertTrue(exception.getMessage().contains("Aucun mot de longueur 30"));
    }
    

/*******************************

          Mode de jeu

 *******************************/


    // Vérifie qu'une partie standard (mode=1) s'initialise correctement.
    @Test
    void startNewGame_standardMode() {
        int length = 5;
        WordleGame game = service.startNewGame(length, 1);
        assertEquals(length, game.getWordLength(), "La longueur du mot doit être celle demandée");
        assertEquals(6, game.getRemainingAttempts(), "Le mode standard doit donner 6 essais");
        assertEquals(1, game.getMode(), "Le mode par défaut doit être 1 (standard)");
    }

    // Vérifie qu'une partie chronométrée (mode=2) s'initialise correctement.
    @Test
    void startNewGame_timerMode() {
        int length = 5;
        WordleGame game = service.startNewGame(length, 2);
        assertEquals(length, game.getWordLength(), "La longueur du mot doit être celle demandée");
        assertEquals(6, game.getRemainingAttempts(), "Le mode chrono doit donner 6 essais");
        assertEquals(2, game.getMode(), "Le mode doit être 2 (chronométré)");
        assertTrue(game.getTimeLimitSeconds() > 0, "Le temps limite doit être supérieur à 0 en mode chronométré");
    }

    // Vérifie qu'une partie pratique (mode=3) s'initialise correctement.
    @Test
    void startNewGame_customMode() {
        int length = 5;
        WordleGame game = service.startNewGame(length, 3); 
        assertEquals(length, game.getWordLength(), "La longueur du mot doit être celle demandée");
        assertEquals(10, game.getRemainingAttempts(), "Le mode pratique doit donner 10 essais");
        assertEquals(3, game.getMode(), "Le mode doit être 3 (pratique)");
    }  


/*******************************

        Logique du jeu

 *******************************/
    
 
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


/*******************************

             Scores

 *******************************/


    // Vérifie que le score est de 1000 quand le mot est trouvé au 1er essai
    @Test
    void score_shouldBeMaxOnFirstTry() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);

        service.checkGuess(game, "APPLE"); // mot correct du 1er coup

        assertEquals(1000, game.getScore(), "Le score devrait être de 1000 au 1er essai");
    }

    // Vérifie que le score diminue avec le nombre d'essais
    @Test
    void score_shouldDecreaseWithAttempts() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);

        service.checkGuess(game, "ALERT");
        service.checkGuess(game, "APPLY");
        service.checkGuess(game, "APPLE"); // trouvé au 3e coup

        assertTrue(game.getScore() < 1000, "Le score devrait être inférieur à 1000 après plusieurs essais");
        assertEquals(1000 - 200, game.getScore(), "Chaque essai supplémentaire doit réduire le score de 100 (sauf le premier)");
    }

    // Vérifie qu'une partie perdue retourne un score >= 0
    @Test
    void score_shouldBeZeroOrPositiveOnLoss() {
        WordleGame game = new WordleGame();
        game.setTargetWord("APPLE");
        game.setWordLength(5);
        game.setRemainingAttempts(1);

        service.checkGuess(game, "LEMON"); // tentative ratée => partie perdue

        assertTrue(game.getScore() >= 0, "Le score ne doit pas être négatif même en cas de défaite");
    }


/*******************************

          Dictionnaire

 *******************************/


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
        Field dictField = WordleService.class.getDeclaredField("fullDictionary");
        dictField.setAccessible(true);
        dictField.set(svc, new ArrayList<>());
    }

}
