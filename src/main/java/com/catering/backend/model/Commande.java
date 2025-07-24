package com.catering.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String numeroCommande;
    @Enumerated(EnumType.STRING)
    private TypeClient typeClient;

    @Enumerated(EnumType.STRING)
    private TypeCommande typeCommande;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    private String nomClient;
    private String salle;
    private int nombreTables;   // ou personnes selon le type
    private LocalDate date;

    private double prixParTable;
    private double total; // calculé après sélection produits + prestataires

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProduitCommande> produits;

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public TypeClient getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(TypeClient typeClient) {
        this.typeClient = typeClient;
    }

    public TypeCommande getTypeCommande() {
        return typeCommande;
    }

    public void setTypeCommande(TypeCommande typeCommande) {
        this.typeCommande = typeCommande;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getPrixParTable() {
        return prixParTable;
    }

    public void setPrixParTable(double prixParTable) {
        this.prixParTable = prixParTable;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<ProduitCommande> getProduits() {
        return produits;
    }

    public void setProduits(List<ProduitCommande> produits) {
        this.produits = produits;
    }
    public String getNumeroCommande() {
        return numeroCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }
}
