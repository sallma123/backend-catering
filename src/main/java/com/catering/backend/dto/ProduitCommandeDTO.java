package com.catering.backend.dto;

public class ProduitCommandeDTO {
    private String nom;
    private String categorie;
    private double prix;
    private boolean selectionne;

    // ✅ Getters
    public String getNom() {
        return nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public double getPrix() {
        return prix;
    }

    public boolean isSelectionne() {
        return selectionne;
    }

    // ✅ Setters
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public void setSelectionne(boolean selectionne) {
        this.selectionne = selectionne;
    }
}
