# Wordle_TU

## Description

Ce projet implémente une version console du jeu Wordle, en Java, avec gestion de longueurs dynamiques, 
différents modes (standard, chronométré, pratique) et un système d’analyse des entrées. 
Le projet inclut également une suite de tests unitaires pour valider son fonctionnement.

## Prérequis
- **Java 17** ou version ultérieure installée sur votre machine.

- **Maven** (version 3.6 ou ultérieure).

- Un éditeur avec support Maven/Java (Visual Studio Code, IntelliJ, Eclipse, etc.).

- (Optionnel) Plugin Spring Boot sur VS Code si vous voulez l’assistance Spring

## Installation & Récupération du projet

Cloner le dépôt depuis **GitHub** :
```
git clone https://github.com/NolanCANO/Wordle_TU.git
```

## Lancement du jeu en console

1. Compiler le projet :
```
mvn clean compile
```

2. Exécuter l’application :
```
mvn exec:java
```

Cette commande lancera la classe GameRunner, qui démarrera le Wordle en mode console.

3. L’application vous guidera alors pour :
- Choisir la **longueur** de mot (selon les min/max du dictionnaire).
- Choisir le **mode** de jeu (standard, chronométré, pratique).
- Deviner le mot en un certain nombre d’essais.

## Lancement des tests unitaires

Pour exécuter l’ensemble de la suite de **tests** JUnit :
```
mvn clean test
```
Vous verrez un récapitulatif du nombre de tests exécutés, le nombre de réussites/échecs, etc.

## Rapport de couverture

Pour générer et consulter un **rapport de couverture** au format HTML :

1. Lancez :
```
mvn clean test jacoco:report
```

2. Ouvrez ensuite le fichier suivant dans votre navigateur :
**target/site/jacoco/index.html**

Vous pourrez y consulter les taux de couverture.

## Structure du projet

- **src/main/java/com/example/wordle/application/**: Contient la classe **GameRunner** (point d’entrée console).
- **src/main/java/com/example/wordle/service/**: Logique du jeu (validation, gestion du dictionnaire, etc.).
- **src/main/java/com/example/wordle/model/**: Modèle métier (**WordleGame**)
- **src/test/java/com/example/wordle/**: Les tests unitaires JUnit (**WordleServiceTest**).

---

By Nolan CANO

