package com.catering.backend.repository;

import com.catering.backend.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    @Query("SELECT COUNT(c) FROM Commande c WHERE YEAR(c.date) = :annee")
    int countByYear(@Param("annee") int annee);

    boolean existsByDate(LocalDate date);

    // 🔁 Pour suppression auto
    List<Commande> findByCorbeilleTrueAndDateSuppressionBefore(LocalDate limite);
}