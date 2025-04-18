package com.example.wordle.application;

import com.example.wordle.model.GameStats;
import com.example.wordle.model.WordleGame;
import com.example.wordle.service.WordleService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

public class GameRunner {

    public static void main(String[] args) {
        // Lance le contexte Spring
        ConfigurableApplicationContext context = SpringApplication.run(WordleApplication.class, args);

        // Récupère WordleService à partir du contexte Spring
        WordleService wordleService = context.getBean(WordleService.class);

        try (Scanner scanner = new Scanner(System.in)) {

            // Récupère la longueur min et max du dictionnaire
            int minLen = wordleService.getMinWordLength();
            int maxLen = wordleService.getMaxWordLength();

            // Boucle pour la longueur de mot
            int wordLength = -1;
            while (wordLength == -1) {
                System.out.printf("Choisissez la longueur du mot (entre %d et %d) : ", minLen, maxLen);
                String input = scanner.nextLine().trim();

                try {
                    int val = Integer.parseInt(input);
                    if (val < minLen || val > maxLen) {
                        System.out.println("Longueur invalide. Veuillez saisir un entier entre "
                                + minLen + " et " + maxLen + ".");
                    } else {
                        wordLength = val;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Saisie invalide. Veuillez entrer un entier.");
                }
            }

            int modeChoice = -1;
            while (modeChoice == -1) {
                System.out.println("Choisissez un mode de jeu:");
                System.out.println("1 - Standard (6 essais)");
                System.out.println("2 - Chronométré (60s)");
                System.out.println("3 - Pratique (10 essais)");
                String input = scanner.nextLine().trim();

                try {
                    modeChoice = Integer.parseInt(input);
                    if (modeChoice < 1 || modeChoice > 3) {
                        System.out.println("Saisie invalide. Veuillez choisir 1, 2 ou 3.");
                        modeChoice = -1;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Saisie invalide. Veuillez entrer 1, 2 ou 3.");
                }
            }

            // Initialiser la partie
            WordleGame game = wordleService.startNewGame(wordLength, modeChoice);

            System.out.printf("Mode choisi : %s, longueur du mot : %d%n",
                    getModeName(modeChoice), wordLength);

            while (!game.isGameOver()) {
                System.out.printf("Essais restants: %d. Entrez un mot (%d lettres): ",
                        game.getRemainingAttempts(), wordLength);
                String guess = scanner.nextLine().trim();

                try {
                    String feedback = wordleService.checkGuess(game, guess);
                    System.out.println(feedback);

                    if (game.isWon()) {
                        System.out.println("Gagné ! Le mot était : " + game.getTargetWord());
                        break;
                    } else if (game.isGameOver()) {
                        // Si on est arrivé ici, c'est qu'il n'y a plus d'essais
                        System.out.println("Perdu ! Le mot était : " + game.getTargetWord());
                    }
                } catch (IllegalStateException e) {
                    // On gère ici l'exception "Temps écoulé !" 
                    if ("Temps écoulé !".equals(e.getMessage())) {
                        System.out.println("Temps écoulé !");
                        // Afficher le mot
                        System.out.println("Le mot était : " + game.getTargetWord());
                        game.setGameOver(true);
                        break;
                    } else {
                        // Si c’est une autre IllegalStateException, on la relance
                        throw e;
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            // Afficher le score de la partie
            System.out.println("Score de cette partie : " + game.getScore());

            GameStats stats = wordleService.getStats();
            System.out.println("\n=== Statistiques ===");
            System.out.println("Parties jouées : " + stats.getTotalGames());
            System.out.println("Victoires      : " + stats.getWins());
            System.out.println("Série actuelle : " + stats.getCurrentStreak());
            System.out.println("Meilleure série: " + stats.getBestStreak());
            System.out.printf("Tentatives moyennes : %.2f%n", stats.getAverageAttempts());
            System.out.printf("Score moyen       : %.2f%n", stats.getAverageScore());
            System.out.println("=== Fin de la partie ===");
        }

        // Ferme proprement le contexte Spring
        context.close();
    }

    private static String getModeName(int choice) {
        switch (choice) {
            case 1: return "Standard";
            case 2: return "Chronométré";
            case 3: return "Pratique";
            default: return "Inconnu";
        }
    }
}
