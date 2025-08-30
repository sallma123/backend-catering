package com.catering.backend.repository;

import com.catering.backend.model.CategorieProduit;
import com.catering.backend.model.TypeCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieProduitRepository extends JpaRepository<CategorieProduit, Long> {
    List<CategorieProduit> findByTypeCommandeOrderByOrdreAffichage(TypeCommande typeCommande);
}

