package com.catering.backend.service;

import com.catering.backend.dto.CommandeDTO;
import com.catering.backend.dto.ProduitCommandeDTO;
import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.repository.CommandeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;

    public CommandeService(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
    }

    public Commande creerCommande(CommandeDTO dto) {
        Commande commande = new Commande();
        commande.setTypeClient(dto.typeClient);
        commande.setTypeCommande(dto.typeCommande);
        commande.setStatut(dto.statut);
        commande.setNomClient(dto.nomClient);
        commande.setSalle(dto.salle);
        commande.setNombreTables(dto.nombreTables);
        commande.setPrixParTable(dto.prixParTable);
        commande.setDate(dto.date);

        List<ProduitCommande> produits = dto.produits.stream()
                .filter(p -> p.selectionne) // on garde que les produits cochés
                .map(p -> {
                    ProduitCommande produit = new ProduitCommande();
                    produit.setNom(p.nom);
                    produit.setCategorie(p.categorie);
                    produit.setPrix(p.prix);
                    produit.setSelectionne(true);
                    produit.setCommande(commande);
                    return produit;
                }).collect(Collectors.toList());

        commande.setProduits(produits);
        commande.setTotal(dto.prixParTable * dto.nombreTables +
                produits.stream().mapToDouble(ProduitCommande::getPrix).sum());

        return commandeRepository.save(commande);
    }

    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    public Commande getCommandeById(Long id) {
        return commandeRepository.findById(id).orElse(null);
    }

    public void supprimerCommande(Long id) {
        commandeRepository.deleteById(id);
    }
}
