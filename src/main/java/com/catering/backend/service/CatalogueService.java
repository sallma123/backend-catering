package com.catering.backend.service;

import com.catering.backend.dto.CategorieProduitDTO;
import com.catering.backend.dto.ProduitDefiniDTO;
import com.catering.backend.mapper.CatalogueMapper;
import com.catering.backend.model.CategorieProduit;
import com.catering.backend.model.ProduitDefini;
import com.catering.backend.model.TypeCommande;
import com.catering.backend.repository.CategorieProduitRepository;
import com.catering.backend.repository.ProduitDefiniRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogueService {

    @Autowired
    private CategorieProduitRepository categorieRepo;
    @Autowired
    private ProduitDefiniRepository produitRepo;

    // 🔹 Lire catalogue
    public List<CategorieProduitDTO> getCatalogue(TypeCommande type) {
        List<CategorieProduit> categories = categorieRepo.findByTypeCommandeOrderByOrdreAffichage(type);
        categories.forEach(cat -> cat.setProduits(produitRepo.findByCategorieOrderByOrdreAffichage(cat)));
        return categories.stream().map(CatalogueMapper::toDto).collect(Collectors.toList());
    }

    // 🔹 Ajouter catégorie
    public CategorieProduitDTO addCategorie(CategorieProduit cat) {
        return CatalogueMapper.toDto(categorieRepo.save(cat));
    }

    // 🔹 Modifier catégorie
    public CategorieProduitDTO updateCategorie(Long id, CategorieProduit cat) {
        cat.setId(id);
        return CatalogueMapper.toDto(categorieRepo.save(cat));
    }

    // 🔹 Supprimer catégorie
    public void deleteCategorie(Long id) {
        categorieRepo.deleteById(id);
    }

    // 🔹 Ajouter produit
    public ProduitDefiniDTO addProduit(Long categorieId, ProduitDefini produit) {
        CategorieProduit cat = categorieRepo.findById(categorieId).orElseThrow();
        produit.setCategorie(cat);
        return CatalogueMapper.toDto(produitRepo.save(produit));
    }

    // 🔹 Modifier produit
    public ProduitDefiniDTO updateProduit(Long id, ProduitDefini produit) {
        ProduitDefini existing = produitRepo.findById(id).orElseThrow();
        produit.setId(id);
        produit.setCategorie(existing.getCategorie());
        return CatalogueMapper.toDto(produitRepo.save(produit));
    }

    // 🔹 Supprimer produit
    public void deleteProduit(Long id) {
        produitRepo.deleteById(id);
    }
}
