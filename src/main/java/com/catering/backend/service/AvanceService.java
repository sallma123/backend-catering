package com.catering.backend.service;

import com.catering.backend.dto.AvanceDTO;
import com.catering.backend.model.Avance;
import com.catering.backend.model.Commande;
import com.catering.backend.repository.AvanceRepository;
import com.catering.backend.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvanceService {

    @Autowired
    private AvanceRepository avanceRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    public AvanceDTO ajouterAvance(Long commandeId, AvanceDTO dto) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        double resteAPayer = commande.getResteAPayer();
        if (dto.getMontant() > resteAPayer) {
            throw new RuntimeException("Le montant dépasse le reste à payer");
        }

        Avance avance = new Avance();
        avance.setMontant(dto.getMontant());
        avance.setDate(dto.getDate());
        avance.setType(dto.getType()); // ✅ sauvegarde du type
        avance.setCommande(commande);

        avanceRepository.save(avance);
        return toDTO(avance);
    }

    public List<AvanceDTO> getAvancesByCommande(Long commandeId) {
        return avanceRepository.findByCommandeId(commandeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AvanceDTO toDTO(Avance avance) {
        AvanceDTO dto = new AvanceDTO();
        dto.setId(avance.getId());
        dto.setMontant(avance.getMontant());
        dto.setDate(avance.getDate());
        dto.setType(avance.getType()); // ✅ mapping type -> DTO
        return dto;
    }

    public void supprimerAvance(Long commandeId, Long avanceId) {
        Avance avance = avanceRepository.findById(avanceId)
                .orElseThrow(() -> new RuntimeException("Avance introuvable"));
        if (!avance.getCommande().getId().equals(commandeId)) {
            throw new RuntimeException("Avance n'appartient pas à la commande");
        }
        avanceRepository.delete(avance);
    }
}

