package com.catering.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
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
    @Column(length = 50)
    private StatutCommande statut;

    private String nomClient;
    private String salle;
    private int nombreTables;   // ou personnes selon le type
    private LocalDate date;
    @Column(length = 1000)
    private String commentaire;
    private double prixParTable;
    private double total; // calculé après sélection produits + prestataires
    private String objet;
    @Column(name = "date_fiche")
    private LocalDate dateFiche;
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Avance> avances = new ArrayList<>();


    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProduitCommande> produits;

    private boolean corbeille = false;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateSuppression; // Date à laquelle elle est mise à la corbeille

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
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }
    public LocalDate getDateFiche() {
        return dateFiche;
    }

    public void setDateFiche(LocalDate dateFiche) {
        this.dateFiche = dateFiche;
    }
    public List<Avance> getAvances() {
        return avances;
    }

    public void setAvances(List<Avance> avances) {
        this.avances = avances;
    }

    public double getResteAPayer() {
        double totalAvances = avances.stream().mapToDouble(Avance::getMontant).sum();
        return total - totalAvances;
    }
    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public boolean isCorbeille() {
        return corbeille;
    }

    public void setCorbeille(boolean corbeille) {
        this.corbeille = corbeille;
    }

    public LocalDate getDateSuppression() {
        return dateSuppression;
    }

    public void setDateSuppression(LocalDate dateSuppression) {
        this.dateSuppression = dateSuppression;
    }

}
