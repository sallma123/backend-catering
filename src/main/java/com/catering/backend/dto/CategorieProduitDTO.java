package com.catering.backend.dto;

import java.util.List;

public class CategorieProduitDTO {
    private Long id;
    private String nom;
    private int ordreAffichage;
    private String typeCommande; // ex: "MARIAGE"
    private List<ProduitDefiniDTO> produits;

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getOrdreAffichage() {
        return ordreAffichage;
    }

    public void setOrdreAffichage(int ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    public String getTypeCommande() {
        return typeCommande;
    }

    public void setTypeCommande(String typeCommande) {
        this.typeCommande = typeCommande;
    }

    public List<ProduitDefiniDTO> getProduits() {
        return produits;
    }

    public void setProduits(List<ProduitDefiniDTO> produits) {
        this.produits = produits;
    }
}
