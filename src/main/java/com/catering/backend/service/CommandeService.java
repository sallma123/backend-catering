package com.catering.backend.service;

import com.catering.backend.dto.CommandeDTO;
import com.catering.backend.dto.ProduitCommandeDTO;
import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.model.StatutCommande;
import com.catering.backend.model.TypeClient;
import com.catering.backend.model.TypeCommande;
import com.catering.backend.repository.CommandeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;

    public CommandeService(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
    }
    public String genererNumeroCommande() {
        int anneeActuelle = LocalDate.now().getYear();

        int count = commandeRepository.countByYear(anneeActuelle);
        int numeroIncremental = count + 1;

        return numeroIncremental + "/" + anneeActuelle;
    }
    public Commande creerCommande(CommandeDTO dto) {
        Commande commande = new Commande();

        // ✅ Conversion des enums depuis les Strings du DTO
        commande.setTypeClient(TypeClient.valueOf(dto.getTypeClient().toUpperCase()));
        commande.setTypeCommande(TypeCommande.valueOf(dto.getTypeCommande().toUpperCase()));
        commande.setStatut(StatutCommande.valueOf(dto.getStatut().toUpperCase()));

        // ✅ Autres champs
        commande.setNomClient(dto.getNomClient());
        commande.setSalle(dto.getSalle());
        commande.setNombreTables(dto.getNombreTables());
        commande.setPrixParTable(dto.getPrixParTable());
        commande.setDate(LocalDate.parse(dto.getDate()));
        commande.setNumeroCommande(genererNumeroCommande());

        // ✅ Transformation des produits cochés
        List<ProduitCommande> produits = dto.getProduits().stream()
                .filter(ProduitCommandeDTO::isSelectionne)
                .map(p -> {
                    ProduitCommande produit = new ProduitCommande();
                    produit.setNom(p.getNom());
                    produit.setCategorie(p.getCategorie());
                    produit.setPrix(p.getPrix());
                    produit.setSelectionne(true);
                    produit.setCommande(commande); // association bidirectionnelle
                    return produit;
                }).collect(Collectors.toList());

        // ✅ Ajout à la commande
        commande.setProduits(produits);

        // ✅ Calcul total
        double totalProduits = produits.stream().mapToDouble(ProduitCommande::getPrix).sum();
        double total = dto.getPrixParTable() * dto.getNombreTables() + totalProduits;
        commande.setTotal(total);

        return commandeRepository.save(commande);
    }

    public List<CommandeDTO> getAllCommandes() {
        return commandeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public Commande getCommandeById(Long id) {
        return commandeRepository.findById(id).orElse(null);
    }

    public void supprimerCommande(Long id) {
        commandeRepository.deleteById(id);
    }
    public CommandeDTO toDTO(Commande commande) {
        CommandeDTO dto = new CommandeDTO();
        dto.setNumeroCommande(commande.getNumeroCommande());
        dto.setNomClient(commande.getNomClient());
        dto.setSalle(commande.getSalle());
        dto.setDate(commande.getDate().toString());
        dto.setNombreTables(commande.getNombreTables());
        dto.setPrixParTable(commande.getPrixParTable());
        dto.setStatut(commande.getStatut().name());
        dto.setTypeClient(commande.getTypeClient().name());
        dto.setTypeCommande(commande.getTypeCommande().name());

        List<ProduitCommandeDTO> produitsDTO = commande.getProduits().stream().map(p -> {
            ProduitCommandeDTO produitDTO = new ProduitCommandeDTO();
            produitDTO.setNom(p.getNom());
            produitDTO.setCategorie(p.getCategorie());
            produitDTO.setPrix(p.getPrix());
            produitDTO.setSelectionne(p.isSelectionne());
            return produitDTO;
        }).collect(Collectors.toList());

        dto.setProduits(produitsDTO);
        return dto;
    }

}
