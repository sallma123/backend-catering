package com.catering.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class ProduitCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String nom;
    private String categorie; // ex: "Réception", "Dîner", etc.
    private double prix;
    @Column(nullable = true)
    private Integer quantite;
    private boolean selectionne;


    @ManyToOne
    @JoinColumn(name = "commande_id")
    @JsonIgnore
    private Commande commande;

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public boolean isSelectionne() {
        return selectionne;
    }

    public void setSelectionne(boolean selectionne) {
        this.selectionne = selectionne;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }
    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

}
