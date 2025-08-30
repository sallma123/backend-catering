package com.catering.backend.controller;

import com.catering.backend.dto.CategorieProduitDTO;
import com.catering.backend.dto.ProduitDefiniDTO;
import com.catering.backend.model.CategorieProduit;
import com.catering.backend.model.ProduitDefini;
import com.catering.backend.model.TypeCommande;
import com.catering.backend.service.CatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogue")
public class CatalogueController {

    @Autowired
    private CatalogueService catalogueService;

    // Lire tout le catalogue pour un type de commande
    @GetMapping("/{typeCommande}")
    public List<CategorieProduitDTO> getCatalogue(@PathVariable TypeCommande typeCommande) {
        return catalogueService.getCatalogue(typeCommande);
    }

    // CRUD Catégorie
    @PostMapping
    public CategorieProduitDTO addCategorie(@RequestBody CategorieProduit categorie) {
        return catalogueService.addCategorie(categorie);
    }

    @PutMapping("/{id}")
    public CategorieProduitDTO updateCategorie(@PathVariable Long id, @RequestBody CategorieProduit categorie) {
        return catalogueService.updateCategorie(id, categorie);
    }

    @DeleteMapping("/{id}")
    public void deleteCategorie(@PathVariable Long id) {
        catalogueService.deleteCategorie(id);
    }

    // CRUD Produits
    @PostMapping("/{categorieId}/produits")
    public ProduitDefiniDTO addProduit(@PathVariable Long categorieId, @RequestBody ProduitDefini produit) {
        return catalogueService.addProduit(categorieId, produit);
    }

    @PutMapping("/produits/{id}")
    public ProduitDefiniDTO updateProduit(@PathVariable Long id, @RequestBody ProduitDefini produit) {
        return catalogueService.updateProduit(id, produit);
    }

    @DeleteMapping("/produits/{id}")
    public void deleteProduit(@PathVariable Long id) {
        catalogueService.deleteProduit(id);
    }
}

