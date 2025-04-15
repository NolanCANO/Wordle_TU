package com.example.wordle.application;

import com.example.wordle.model.WordleGame;
import com.example.wordle.service.WordleService;
import java.util.Scanner;

public class GameRunner {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WordleService wordleService = new WordleService();

        System.out.println("=== Bienvenue dans Wordle (Terminal) ===");
        System.out.println("Choisissez la longueur du mot (ex: 5, 6, 7...): ");
        int wordLength = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Choisissez un mode de jeu:");
        System.out.println("1 - Standard (6 essais)");
        System.out.println("2 - Chronométré (60s)");
        System.out.println("3 - Pratique (plus d'essais)");
        int modeChoice = Integer.parseInt(scanner.nextLine().trim());

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
            }
            catch (IllegalStateException e) {
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
            }
            catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        // Fin de la partie
        System.out.println("=== Fin de la partie ===");
        scanner.close();
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
