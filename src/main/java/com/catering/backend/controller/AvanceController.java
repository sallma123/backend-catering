package com.catering.backend.controller;

import com.catering.backend.dto.AvanceDTO;
import com.catering.backend.service.AvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin
public class AvanceController {

    @Autowired
    private AvanceService avanceService;

    @PostMapping("/{commandeId}/avances")
    public AvanceDTO ajouterAvance(@PathVariable Long commandeId, @RequestBody AvanceDTO dto) {
        return avanceService.ajouterAvance(commandeId, dto);
    }

    @GetMapping("/{commandeId}/avances")
    public List<AvanceDTO> getAvances(@PathVariable Long commandeId) {
        return avanceService.getAvancesByCommande(commandeId);
    }
    @DeleteMapping("/{commandeId}/avances/{avanceId}")
    public void supprimerAvance(@PathVariable Long commandeId, @PathVariable Long avanceId) {
        avanceService.supprimerAvance(commandeId, avanceId);
    }

}
