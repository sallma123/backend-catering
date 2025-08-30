package com.catering.backend.repository;

import com.catering.backend.model.CategorieProduit;
import com.catering.backend.model.ProduitDefini;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProduitDefiniRepository extends JpaRepository<ProduitDefini, Long> {
    List<ProduitDefini> findByCategorieOrderByOrdreAffichage(CategorieProduit categorie);
}

