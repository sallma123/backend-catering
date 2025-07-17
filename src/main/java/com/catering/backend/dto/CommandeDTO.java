package com.catering.backend.dto;

import com.catering.backend.model.TypeClient;
import com.catering.backend.model.TypeCommande;
import com.catering.backend.model.StatutCommande;

import java.time.LocalDate;
import java.util.List;

public class CommandeDTO {
    public TypeClient typeClient;
    public TypeCommande typeCommande;
    public StatutCommande statut;
    public String nomClient;
    public String salle;
    public int nombreTables;
    public double prixParTable;
    public LocalDate date;

    public List<ProduitCommandeDTO> produits;
}
