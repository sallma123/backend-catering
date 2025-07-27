package com.catering.backend.controller;

import com.catering.backend.dto.CommandeDTO;
import com.catering.backend.model.Commande;
import com.catering.backend.service.CommandeService;
import com.catering.backend.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
public class CommandeController {

    private final CommandeService commandeService;
    private final PdfService pdfService;

    public CommandeController(CommandeService commandeService, PdfService pdfService) {
        this.commandeService = commandeService;
        this.pdfService = pdfService;
    }

    // ✅ Créer une commande
    @PostMapping
    public Commande creerCommande(@RequestBody CommandeDTO commandeDTO) {
        return commandeService.creerCommande(commandeDTO);
    }

    // ✅ Récupérer toutes les commandes
    @GetMapping
    public List<CommandeDTO> getAllCommandes() {
        return commandeService.getAllCommandes();
    }


    // ✅ Récupérer une commande par ID
    @GetMapping("/{id}")
    public CommandeDTO getCommandeById(@PathVariable Long id) {
        return commandeService.toDTO(commandeService.getCommandeById(id));
    }


    // ✅ Supprimer une commande
    @DeleteMapping("/{id}")
    public void deleteCommande(@PathVariable Long id) {
        commandeService.supprimerCommande(id);
    }

    // ✅ Télécharger la fiche PDF
    @GetMapping("/{id}/fiche")
    public ResponseEntity<byte[]> genererFiche(@PathVariable Long id) {
        try {
            byte[] pdf = pdfService.genererFicheCommande(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "fiche_commande_" + id + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    // ✅ Modifier une commande existante
    @PutMapping("/{id}")
    public Commande modifierCommande(@PathVariable Long id, @RequestBody CommandeDTO commandeDTO) {
        return commandeService.modifierCommande(id, commandeDTO);
    }


}
