package com.catering.backend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class CategorieProduit {
    @Id
    @GeneratedValue
    private Long id;

    private String nom;              // ex : "Côté sucré"
    private int ordreAffichage;      // ex : 1, 2, 3 pour trier

    @Enumerated(EnumType.STRING)
    private TypeCommande typeCommande;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProduitDefini> produits;

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

    public TypeCommande getTypeCommande() {
        return typeCommande;
    }

    public void setTypeCommande(TypeCommande typeCommande) {
        this.typeCommande = typeCommande;
    }

    public List<ProduitDefini> getProduits() {
        return produits;
    }

    public void setProduits(List<ProduitDefini> produits) {
        this.produits = produits;
    }
}
