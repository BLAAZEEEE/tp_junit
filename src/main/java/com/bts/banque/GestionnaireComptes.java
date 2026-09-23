package com.bts.banque;

import com.bts.banque.exceptions.CompteDejaExistantException;
import com.bts.banque.exceptions.CompteInconnuException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestionnaireComptes {

    private final Map<String, CompteBancaire> comptes = new HashMap<>();

    public void ajouterCompte(CompteBancaire compte) {
        if (comptes.containsKey(compte.getIban())) {
            throw new CompteDejaExistantException("Un compte existe déjà pour l'IBAN " + compte.getIban());
        }
        comptes.put(compte.getIban(), compte);
    }

    public CompteBancaire rechercherCompte(String iban) {
        CompteBancaire compte = comptes.get(iban);
        if (compte == null) {
            throw new CompteInconnuException("Aucun compte trouvé pour l'IBAN " + iban);
        }
        return compte;
    }

    public void virement(String ibanSource, String ibanDestination, double montant) {
        CompteBancaire source = rechercherCompte(ibanSource);
        CompteBancaire destination = rechercherCompte(ibanDestination);
        source.retirer(montant);
        destination.deposer(montant);
    }

    public double soldeTotal() {
        return comptes.values().stream()
                .mapToDouble(CompteBancaire::getSolde)
                .sum();
    }

    public List<CompteBancaire> listeComptesEnDecouvert() {
        return comptes.values().stream()
                .filter(CompteBancaire::estEnDecouvert)
                .collect(Collectors.toList());
    }
}
