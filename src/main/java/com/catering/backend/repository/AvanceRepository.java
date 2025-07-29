package com.catering.backend.repository;

import com.catering.backend.model.Avance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvanceRepository extends JpaRepository<Avance, Long> {
    List<Avance> findByCommandeId(Long commandeId);
}
