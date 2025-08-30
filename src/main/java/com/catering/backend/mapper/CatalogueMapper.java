package com.catering.backend.mapper;

import com.catering.backend.dto.CategorieProduitDTO;
import com.catering.backend.dto.ProduitDefiniDTO;
import com.catering.backend.model.CategorieProduit;
import com.catering.backend.model.ProduitDefini;

import java.util.stream.Collectors;

public class CatalogueMapper {

    public static CategorieProduitDTO toDto(CategorieProduit entity) {
        CategorieProduitDTO dto = new CategorieProduitDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setOrdreAffichage(entity.getOrdreAffichage());
        dto.setTypeCommande(entity.getTypeCommande().name());

        dto.setProduits(entity.getProduits().stream()
                .map(CatalogueMapper::toDto)
                .collect(Collectors.toList()));

        return dto;
    }

    public static ProduitDefiniDTO toDto(ProduitDefini entity) {
        ProduitDefiniDTO dto = new ProduitDefiniDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setOrdreAffichage(entity.getOrdreAffichage());
        dto.setCategorieId(entity.getCategorie().getId());
        return dto;
    }

    public static ProduitDefini toEntity(ProduitDefiniDTO dto, CategorieProduit categorie) {
        ProduitDefini produit = new ProduitDefini();
        produit.setId(dto.getId());
        produit.setNom(dto.getNom());
        produit.setOrdreAffichage(dto.getOrdreAffichage());
        produit.setCategorie(categorie);
        return produit;
    }
}
