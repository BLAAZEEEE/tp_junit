package com.bts.banque;

import com.bts.banque.exceptions.CompteDejaExistantException;
import com.bts.banque.exceptions.CompteInconnuException;
import com.bts.banque.exceptions.SoldeInsuffisantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestionnaireComptesTest {

    private static final double DELTA = 0.0001;

    private GestionnaireComptes gestionnaire;
    private CompteBancaire compteA;
    private CompteBancaire compteB;

    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireComptes();
        compteA = new CompteBancaire("FR0001", "Alice", 50);
        compteB = new CompteBancaire("FR0002", "Bob", 50);
        gestionnaire.ajouterCompte(compteA);
        gestionnaire.ajouterCompte(compteB);
        compteA.deposer(300);
    }

    @Nested
    @DisplayName("Cas nominaux")
    class CasNominaux {

        @Test
        @DisplayName("rechercherCompte retrouve un compte existant par son IBAN")
        void rechercherCompteExistant() {
            assertSame(compteA, gestionnaire.rechercherCompte("FR0001"));
        }

        @Test
        @DisplayName("Un virement réussi débite la source et crédite la destination")
        void virementReussi() {
            gestionnaire.virement("FR0001", "FR0002", 100);
            assertEquals(200, compteA.getSolde(), DELTA);
            assertEquals(100, compteB.getSolde(), DELTA);
        }

        @Test
        @DisplayName("soldeTotal fait la somme des soldes de tous les comptes gérés")
        void soldeTotalCorrect() {
            assertEquals(300, gestionnaire.soldeTotal(), DELTA);
        }
    }

    @Nested
    @DisplayName("Cas d'erreur")
    class CasErreur {

        @Test
        @DisplayName("Ajouter un compte avec un IBAN déjà existant lève CompteDejaExistantException")
        void ajoutIbanExistantLeveException() {
            CompteBancaire doublon = new CompteBancaire("FR0001", "Charlie");
            assertThrows(CompteDejaExistantException.class, () -> gestionnaire.ajouterCompte(doublon));
        }

        @Test
        @DisplayName("Rechercher un IBAN inconnu lève CompteInconnuException")
        void rechercheIbanInconnuLeveException() {
            assertThrows(CompteInconnuException.class, () -> gestionnaire.rechercherCompte("FR9999"));
        }

        @Test
        @DisplayName("Un virement depuis un IBAN inconnu lève CompteInconnuException")
        void virementDepuisIbanInconnuLeveException() {
            assertThrows(CompteInconnuException.class,
                    () -> gestionnaire.virement("INCONNU", "FR0002", 10));
        }

        @Test
        @DisplayName("Un virement vers un IBAN inconnu lève CompteInconnuException")
        void virementVersIbanInconnuLeveException() {
            assertThrows(CompteInconnuException.class,
                    () -> gestionnaire.virement("FR0001", "INCONNU", 10));
        }

        @Test
        @DisplayName("Un virement qui échoue au milieu ne modifie le solde d'aucun compte")
        void virementEchoueNeModifieAucunSolde() {
            double soldeADepart = compteA.getSolde();
            double soldeBDepart = compteB.getSolde();

            assertThrows(SoldeInsuffisantException.class,
                    () -> gestionnaire.virement("FR0001", "FR0002", 10_000));

            assertEquals(soldeADepart, compteA.getSolde(), DELTA);
            assertEquals(soldeBDepart, compteB.getSolde(), DELTA);
        }
    }

    @Test
    @DisplayName("listeComptesEnDecouvert ne retourne que les comptes à solde négatif")
    void listeComptesEnDecouvertCorrecte() {
        compteB.retirer(20);

        List<CompteBancaire> comptesEnDecouvert = gestionnaire.listeComptesEnDecouvert();

        assertEquals(1, comptesEnDecouvert.size());
        assertSame(compteB, comptesEnDecouvert.get(0));
        assertTrue(comptesEnDecouvert.get(0).estEnDecouvert());
    }
}
