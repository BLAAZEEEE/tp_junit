package com.bts.banque;

import com.bts.banque.exceptions.MontantInvalideException;
import com.bts.banque.exceptions.SoldeInsuffisantException;

import java.util.UUID;

public class CompteBancaire {

    private final String iban;
    private final String titulaire;
    private double solde;
    private final double decouvertAutorise;

    public CompteBancaire(String titulaire) {
        this(UUID.randomUUID().toString(), titulaire, 0);
    }

    public CompteBancaire(String iban, String titulaire) {
        this(iban, titulaire, 0);
    }

    public CompteBancaire(String iban, String titulaire, double decouvertAutorise) {
        this.iban = iban;
        this.titulaire = titulaire;
        this.decouvertAutorise = decouvertAutorise;
        this.solde = 0;
    }

    public void deposer(double montant) {
        if (montant <= 0) {
            throw new MontantInvalideException("Le montant à déposer doit être strictement positif : " + montant);
        }
        solde += montant;
    }

    public void retirer(double montant) {
        if (montant <= 0) {
            throw new MontantInvalideException("Le montant à retirer doit être strictement positif : " + montant);
        }
        if (solde - montant < -decouvertAutorise) {
            throw new SoldeInsuffisantException(
                    "Solde insuffisant : solde=" + solde
                            + ", découvert autorisé=" + decouvertAutorise
                            + ", montant demandé=" + montant);
        }
        solde -= montant;
    }

    public double calculerInterets(double taux) {
        if (taux < 0) {
            throw new MontantInvalideException("Le taux d'intérêt ne peut pas être négatif : " + taux);
        }
        if (solde <= 0) {
            return 0;
        }
        return solde * taux;
    }

    public boolean estEnDecouvert() {
        return solde < 0;
    }

    public double getSolde() {
        return solde;
    }

    public String getTitulaire() {
        return titulaire;
    }

    public String getIban() {
        return iban;
    }

    public double getDecouvertAutorise() {
        return decouvertAutorise;
    }
}
