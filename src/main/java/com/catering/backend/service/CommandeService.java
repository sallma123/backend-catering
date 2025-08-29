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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;

    public CommandeService(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
    }

    public String genererNumeroCommandeUnique() {
        int anneeActuelle = LocalDate.now().getYear();
        int compteur = 1;
        String numero;

        do {
            numero = compteur + "/" + anneeActuelle;
            compteur++;
        } while (commandeRepository.existsByNumeroCommande(numero));

        return numero;
    }


    public Commande creerCommande(CommandeDTO dto) {
        Commande commande = new Commande();

        commande.setTypeClient(TypeClient.valueOf(dto.getTypeClient().toUpperCase()));
        commande.setTypeCommande(TypeCommande.valueOf(dto.getTypeCommande().toUpperCase()));
        commande.setStatut(StatutCommande.valueOf(dto.getStatut().toUpperCase()));
        commande.setNomClient(dto.getNomClient());
        commande.setSalle(dto.getSalle());
        commande.setNombreTables(dto.getNombreTables());
        commande.setPrixParTable(dto.getPrixParTable());
        commande.setDate(LocalDate.parse(dto.getDate()));
        commande.setNumeroCommande(genererNumeroCommandeUnique());
        commande.setObjet(dto.getObjet());
        commande.setCommentaire(dto.getCommentaire());
        commande.setAfficherSignatureCachet(dto.getSignatureCachet() != null && dto.getSignatureCachet());




        commande.setDateFiche(LocalDate.now()); // ✅ initialise une seule fois



        List<ProduitCommande> produits = dto.getProduits().stream()
                .filter(ProduitCommandeDTO::isSelectionne)
                .map(p -> {
                    ProduitCommande produit = new ProduitCommande();
                    produit.setNom(p.getNom());
                    produit.setCategorie(p.getCategorie());
                    produit.setPrix(p.getPrix());
                    produit.setSelectionne(true);
                    produit.setQuantite(p.getQuantite() != null ? p.getQuantite() : 1); // ✅ quantité
                    produit.setCommande(commande);
                    return produit;
                }).collect(Collectors.toList());

        commande.setProduits(produits);

        double totalProduits = produits.stream()
                .mapToDouble(p -> p.getPrix() * p.getQuantite())
                .sum();
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
        dto.setId(commande.getId());
        dto.setNumeroCommande(commande.getNumeroCommande());
        dto.setNomClient(commande.getNomClient());
        dto.setSalle(commande.getSalle());
        dto.setDate(commande.getDate().toString());
        dto.setNombreTables(commande.getNombreTables());
        dto.setPrixParTable(commande.getPrixParTable());
        dto.setStatut(commande.getStatut().name());
        dto.setTypeClient(commande.getTypeClient().name());
        dto.setTypeCommande(commande.getTypeCommande().name());
        dto.setObjet(commande.getObjet());
        dto.setTotal(commande.getTotal());
        dto.setTotal(commande.getTotal());
        dto.setCommentaire(commande.getCommentaire());
        dto.setCorbeille(commande.isCorbeille());
        dto.setSignatureCachet(commande.isAfficherSignatureCachet());


        List<ProduitCommandeDTO> produitsDTO = commande.getProduits().stream().map(p -> {
            ProduitCommandeDTO produitDTO = new ProduitCommandeDTO();
            produitDTO.setNom(p.getNom());
            produitDTO.setCategorie(p.getCategorie());
            produitDTO.setPrix(p.getPrix());
            produitDTO.setSelectionne(p.isSelectionne());
            produitDTO.setQuantite(p.getQuantite()); // ✅ ajout de la quantité
            return produitDTO;
        }).collect(Collectors.toList());

        dto.setProduits(produitsDTO);
        return dto;
    }

    @Transactional
    public Commande modifierCommande(Long id, CommandeDTO dto) {
        Commande existing = getCommandeById(id);

        // mise à jour des champs simples
        existing.setNomClient(dto.getNomClient());
        existing.setSalle(dto.getSalle());
        existing.setNombreTables(dto.getNombreTables());
        existing.setPrixParTable(dto.getPrixParTable());
        existing.setTypeCommande(TypeCommande.valueOf(dto.getTypeCommande().toUpperCase()));
        existing.setStatut(StatutCommande.valueOf(dto.getStatut().toUpperCase()));
        existing.setDate(LocalDate.parse(dto.getDate()));
        existing.setObjet(dto.getObjet());
        existing.setCommentaire(dto.getCommentaire());
        existing.setAfficherSignatureCachet(dto.getSignatureCachet() != null && dto.getSignatureCachet());


        // suppression des produits existants via le repository
        existing.getProduits().forEach(p -> p.setCommande(null)); // détacher la relation
        existing.getProduits().clear();

        List<ProduitCommande> nouveauxProduits = dto.getProduits().stream()
                .filter(ProduitCommandeDTO::isSelectionne)
                .map(p -> {
                    ProduitCommande produit = new ProduitCommande();
                    produit.setNom(p.getNom());
                    produit.setCategorie(p.getCategorie());
                    produit.setPrix(p.getPrix());
                    produit.setSelectionne(true);
                    produit.setQuantite(p.getQuantite() != null ? p.getQuantite() : 1);
                    produit.setCommande(existing);
                    return produit;
                }).collect(Collectors.toList());

        existing.getProduits().addAll(nouveauxProduits);

        double totalProduits = nouveauxProduits.stream()
                .mapToDouble(p -> p.getPrix() * p.getQuantite())
                .sum();
        double total = dto.getPrixParTable() * dto.getNombreTables() + totalProduits;
        existing.setTotal(total);

        return commandeRepository.save(existing);
    }

    public boolean existeCommandeLe(String dateString) {
        try {
            // 🔧 Supprimer les espaces et sauts de ligne
            dateString = dateString.trim();
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return commandeRepository.existsByDate(date);
        } catch (Exception e) {
            throw new RuntimeException("Format de date invalide : " + dateString, e);
        }
    }

    public void mettreEnCorbeille(Long id) {
        Commande commande = getCommandeById(id);
        commande.setCorbeille(true);
        commande.setDateSuppression(LocalDate.now());
        commandeRepository.save(commande);
    }

    public void restaurerCommande(Long id) {
        Commande commande = getCommandeById(id);
        commande.setCorbeille(false);
        commande.setDateSuppression(null);
        commandeRepository.save(commande);
    }

    @Scheduled(cron = "0 0 2 * * *") // chaque jour à 2h
    @Transactional
    public void supprimerCommandesAnciennes() {
        LocalDate limite = LocalDate.now().minusDays(30);
        List<Commande> anciennes = commandeRepository.findByCorbeilleTrueAndDateSuppressionBefore(limite);
        commandeRepository.deleteAll(anciennes);
    }
    public List<CommandeDTO> getCommandesDansCorbeille() {
        return commandeRepository.findAll().stream()
                .filter(Commande::isCorbeille)  // ✅ méthode getter correcte
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}