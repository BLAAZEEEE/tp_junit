package com.bts.banque;

import com.bts.banque.exceptions.MontantInvalideException;
import com.bts.banque.exceptions.SoldeInsuffisantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompteBancaireTest {

    private static final double DELTA = 0.0001;

    private CompteBancaire compte;

    @BeforeEach
    void setUp() {
        compte = new CompteBancaire("FR7612345987650123456789014", "Alice Dupont", 100);
    }

    @Nested
    @DisplayName("Cas nominaux")
    class CasNominaux {

        @Test
        @DisplayName("Un dépôt augmente le solde du montant déposé")
        void deposerAugmenteLeSolde() {
            compte.deposer(50);
            assertEquals(50, compte.getSolde(), DELTA);
        }

        @Test
        @DisplayName("Un retrait diminue le solde du montant retiré")
        void retirerDiminueLeSolde() {
            compte.deposer(200);
            compte.retirer(80);
            assertEquals(120, compte.getSolde(), DELTA);
        }

        @Test
        @DisplayName("Le calcul des intérêts sur un solde positif est correct et ne modifie pas le solde")
        void calculerInteretsSoldePositif() {
            compte.deposer(1000);
            double interets = compte.calculerInterets(0.05);
            assertEquals(50, interets, DELTA);
            assertEquals(1000, compte.getSolde(), DELTA);
        }
    }

    @Nested
    @DisplayName("Cas limites")
    class CasLimites {

        @Test
        @DisplayName("Un retrait qui amène exactement au découvert autorisé passe")
        void retraitExactementAuDecouvertAutorise() {
            compte.retirer(100);
            assertEquals(-100, compte.getSolde(), DELTA);
        }

        @Test
        @DisplayName("Un retrait d'un centime de plus que le découvert autorisé lève une exception")
        void retraitUnCentimeDePlusQueLeDecouvert() {
            assertThrows(SoldeInsuffisantException.class, () -> compte.retirer(100.01));
        }

        @Test
        @DisplayName("Un dépôt de zéro est rejeté")
        void depotDeZeroRejete() {
            assertThrows(MontantInvalideException.class, () -> compte.deposer(0));
        }

        @Test
        @DisplayName("Un retrait de zéro est rejeté")
        void retraitDeZeroRejete() {
            assertThrows(MontantInvalideException.class, () -> compte.retirer(0));
        }
    }

    @Nested
    @DisplayName("Cas d'erreur")
    class CasErreur {

        @Test
        @DisplayName("Un dépôt négatif lève MontantInvalideException")
        void depotNegatifLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.deposer(-10));
        }

        @Test
        @DisplayName("Un retrait négatif lève MontantInvalideException")
        void retraitNegatifLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.retirer(-10));
        }

        @Test
        @DisplayName("Un retrait dépassant le découvert autorisé lève SoldeInsuffisantException")
        void retraitDepassantDecouvertLeveException() {
            assertThrows(SoldeInsuffisantException.class, () -> compte.retirer(500));
        }

        @Test
        @DisplayName("Un taux d'intérêt négatif lève MontantInvalideException")
        void tauxInteretNegatifLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.calculerInterets(-0.01));
        }
    }

    @Test
    @DisplayName("estEnDecouvert retourne true quand le solde est négatif")
    void estEnDecouvertVraiQuandSoldeNegatif() {
        compte.retirer(50);
        assertTrue(compte.estEnDecouvert());
    }

    @Test
    @DisplayName("estEnDecouvert retourne false quand le solde est positif")
    void estEnDecouvertFauxQuandSoldePositif() {
        compte.deposer(10);
        assertFalse(compte.estEnDecouvert());
    }

    @Test
    @DisplayName("calculerInterets retourne 0 quand le solde est négatif")
    void calculerInteretsSoldeNegatifRetourneZero() {
        compte.retirer(50);
        assertEquals(0, compte.calculerInterets(0.1), DELTA);
    }

    @Test
    @DisplayName("Les accesseurs iban et titulaire retournent les valeurs fournies au constructeur")
    void accesseursRetournentLesBonnesValeurs() {
        assertEquals("FR7612345987650123456789014", compte.getIban());
        assertEquals("Alice Dupont", compte.getTitulaire());
    }
}
