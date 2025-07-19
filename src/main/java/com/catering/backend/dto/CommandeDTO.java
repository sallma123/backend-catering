package com.catering.backend.dto;

import java.util.List;

public class CommandeDTO {

    private String typeClient;        // ex: "PARTICULIER", "ENTREPRISE"
    private String typeCommande;      // ex: "MARIAGE", "BUFFET"
    private String statut;            // ex: "NON_PAYEE", "PAYEE"

    private String nomClient;
    private String salle;
    private int nombreTables;
    private double prixParTable;

    private String date; // ✅ Nouvelle propriété au format "yyyy-MM-dd"

    private List<ProduitCommandeDTO> produits;

    // --- Getters & Setters ---
    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getTypeCommande() {
        return typeCommande;
    }

    public void setTypeCommande(String typeCommande) {
        this.typeCommande = typeCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public int getNombreTables() {
        return nombreTables;
    }

    public void setNombreTables(int nombreTables) {
        this.nombreTables = nombreTables;
    }

    public double getPrixParTable() {
        return prixParTable;
    }

    public void setPrixParTable(double prixParTable) {
        this.prixParTable = prixParTable;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<ProduitCommandeDTO> getProduits() {
        return produits;
    }

    public void setProduits(List<ProduitCommandeDTO> produits) {
        this.produits = produits;
    }
}
