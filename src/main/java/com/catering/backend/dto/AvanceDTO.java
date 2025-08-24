package com.catering.backend.dto;

import java.time.LocalDate;

public class AvanceDTO {
    private Long id;
    private Double montant;
    private LocalDate date;
    private String type; // ✅ nouveau champ

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getType() { return type; }   // ✅ getter
    public void setType(String type) { this.type = type; } // ✅ setter
}

