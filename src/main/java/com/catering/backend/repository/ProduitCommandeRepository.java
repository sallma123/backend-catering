package com.catering.backend.repository;

import com.catering.backend.model.ProduitCommande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduitCommandeRepository extends JpaRepository<ProduitCommande, Long> {
}
