# TP JUnit — Gestion de comptes bancaires

## 1. Présentation

Le projet, c'est un petit système de gestion de comptes bancaires avec deux classes :

- `CompteBancaire` : un compte avec un IBAN, un titulaire, un solde et un
  découvert autorisé. On peut déposer, retirer, et calculer des intérêts
  dessus.
- `GestionnaireComptes` : ça gère plusieurs comptes en même temps (ajout,
  recherche par IBAN, virement entre deux comptes, solde total, liste des
  comptes en découvert).

L'idée du TP c'était pas juste de faire marcher le code, mais surtout
d'écrire une vraie suite de tests JUnit qui couvre les cas normaux, les cas
limites et les erreurs, avec des commits Git réguliers plutôt qu'un seul
commit à la fin.

## 2. Choix de conception

- **Pourquoi deux classes séparées ?** `CompteBancaire` ne gère que son
  propre solde, il ne sait même pas que d'autres comptes existent.
  `GestionnaireComptes` s'occupe de tout ce qui implique plusieurs comptes
  (recherche, virement, totaux) mais ne refait jamais les vérifications de
  dépôt/retrait lui-même : il laisse `CompteBancaire` s'en charger. Comme ça
  la règle du découvert autorisé n'existe qu'à un seul endroit dans le code.
- **Les exceptions maison** : j'ai créé 4 exceptions (`MontantInvalideException`,
  `SoldeInsuffisantException`, `CompteDejaExistantException`,
  `CompteInconnuException`), toutes en `RuntimeException` (donc non
  vérifiées). Ce sont des erreurs "métier" (montant invalide, compte
  inconnu...), pas des erreurs techniques genre fichier introuvable, donc ça
  ne me semblait pas utile d'obliger un `try/catch` partout où on les
  utilise.
- **Le virement atomique** : dans `virement()`, je fais d'abord `retirer()`
  sur le compte source, et seulement après `deposer()` sur le compte
  destination. Comme `retirer()` vérifie toutes ses conditions (montant
  positif, découvert respecté) avant de toucher au solde, si ça plante ça
  plante avant d'avoir touché à quoi que ce soit — et `deposer()` n'est
  jamais appelé. Pas besoin d'un `try/catch` compliqué pour "annuler" quoi
  que ce soit, l'ordre des deux appels suffit.
- **TDD ou pas ?** J'ai pas fait de TDD à la lettre. J'ai écrit le code de
  chaque classe d'abord (en suivant le tableau du sujet avec les méthodes
  attendues), puis les tests juste après, classe par classe. Le sujet donnait
  déjà les noms des méthodes, les types et les exceptions attendues, donc il
  n'y avait pas vraiment d'incertitude de conception à lever avec du TDD —
  ça m'a semblé plus rapide d'écrire d'abord le comportement puis de le
  vérifier avec les tests.

## 3. Comment lancer les tests

Avec Maven (le `pom.xml` est déjà configuré avec JUnit 5.10.2) :

```bash
mvn test
```

Depuis IntelliJ, on peut aussi faire clic droit sur `src/test/java` puis
"Run All Tests".

## 4. Récapitulatif des tests

| Classe de test              | Nb de tests | Ce qu'elle teste |
|------------------------------|:-----------:|--------------------|
| `CompteBancaireTest`         | 15          | Dépôt/retrait normaux, calcul d'intérêts (solde positif et négatif), retrait qui tombe pile sur le découvert autorisé, un centime de trop, montants à zéro, montants négatifs, taux négatif, `estEnDecouvert`, les getters. |
| `GestionnaireComptesTest`    | 9           | Recherche par IBAN, virement qui réussit, solde total, ajout d'un IBAN qui existe déjà, recherche/virement sur un IBAN inconnu, virement qui échoue en cours de route (aucun solde ne doit bouger), liste des comptes en découvert. |
| **Total**                    | **24**      | Cas normaux, cas limites et cas d'erreur pour les deux classes. |

Tous les tests passent. Comme je n'avais pas Maven en ligne de commande
pendant le dev, je les ai aussi fait tourner avec le JUnit Console Launcher
pour vérifier au fur et à mesure.

## 5. Difficultés rencontrées

1. **Le `<` vs `<=` sur le découvert autorisé.** Le sujet dit que le solde
   peut descendre "jusqu'à -decouvertAutorise, pas en dessous". Au début
   j'avais mis `<=` dans la condition et ça bloquait le cas limite où le
   retrait tombe pile sur la limite (qui doit pourtant passer). Il fallait
   `solde - montant < -decouvertAutorise` en strict, pas `<=`.
2. **Le cas du montant à zéro.** Le sujet range "dépôt/retrait de zéro" dans
   les cas limites, ce qui m'a d'abord fait penser que ça devait passer.
   Mais la règle dit bien "rejette un montant négatif ou nul", donc zéro
   compte comme "nul" et doit être rejeté avec une exception. Le test
   vérifie donc que ça lève bien `MontantInvalideException`, pas que le
   dépôt fonctionne.
3. **Éviter de complexifier le virement pour rien.** J'ai d'abord pensé
   devoir écrire un `try/catch` pour "annuler" le retrait si le dépôt
   plantait après. En fait il suffit d'appeler `retirer()` avant `deposer()`
   : comme `retirer()` valide tout avant de modifier le solde, si ça plante
   ça plante avant que quoi que ce soit ait changé, donc pas besoin de code
   de rattrapage.

## 6. Bilan

Ce TP m'a surtout appris à tester les limites d'une règle, pas juste le cas
"normal" : c'est exactement sur la frontière (pile sur le découvert, ou un
centime après) que les bugs de `<` vs `<=` se cachent, et un test qui ne
teste que le cas moyen ne les aurait jamais trouvés. Découper les tests avec
`@Nested` (nominaux / limites / erreurs) m'a aussi aidé à vérifier que
j'avais bien un test pour chaque point du sujet, plutôt que de tester un peu
au hasard.
