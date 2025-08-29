package com.catering.backend.dto;
import java.time.LocalDateTime;
import java.util.List;

public class CommandeDTO {

    private Long id; // ✅ Ajout du champ ID

    private String numeroCommande;
    private String typeClient;
    private String typeCommande;
    private String statut;
    private double total;
    private String nomClient;
    private String salle;
    private int nombreTables;
    private double prixParTable;
    private String objet;
    private String date;
    private List<ProduitCommandeDTO> produits;
    private String commentaire;
    private Boolean corbeille;
    private Boolean signatureCachet;
    private LocalDateTime lastUpdatedDate;


    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getNumeroCommande() {
        return numeroCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total=total;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    public Boolean getCorbeille() {
        return corbeille;
    }
    public void setCorbeille(Boolean corbeille) {
        this.corbeille = corbeille;
    }
    public Boolean getSignatureCachet() {
        return signatureCachet;
    }
    public void setSignatureCachet(Boolean signatureCachet) {
        this.signatureCachet = signatureCachet;
    }
    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }
    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
