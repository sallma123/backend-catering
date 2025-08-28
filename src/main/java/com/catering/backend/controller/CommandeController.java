package com.catering.backend.controller;

import com.catering.backend.dto.CommandeDTO;
import com.catering.backend.model.Commande;
import com.catering.backend.service.CommandeService;
import com.catering.backend.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    // ✅ Télécharger la fiche PDF
// ✅ Télécharger la fiche PDF
    @GetMapping("/{id}/fiche")
    public ResponseEntity<byte[]> genererFiche(@PathVariable Long id) {
        try {
            byte[] pdf = pdfService.genererFicheCommande(id);

            // Récupérer la commande pour construire le nom du fichier
            Commande commande = commandeService.getCommandeById(id);

            // JJMM → date événement
            String jjmm = (commande.getDate() != null)
                    ? String.format("%02d%02d", commande.getDate().getDayOfMonth(), commande.getDate().getMonthValue())
                    : "0000";

            // Type de commande (mariage, anniversaire…)
            String typeCommande = (commande.getTypeCommande() != null)
                    ? commande.getTypeCommande().name().toLowerCase()
                    : "commande";

            // Date de création de la fiche
            String dateFiche = (commande.getDateFiche() != null)
                    ? String.format("%02d%02d%04d",
                    commande.getDateFiche().getDayOfMonth(),
                    commande.getDateFiche().getMonthValue(),
                    commande.getDateFiche().getYear())
                    : "00000000";

            // Nom final du fichier
            String filename = String.format("fiche_%s_%s_%s.pdf", jjmm, typeCommande, dateFiche);

            // 🔹 Vérification/log en console
            System.out.println(">>> Nom du fichier généré : " + filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // 🔹 pour voir l'erreur exacte si ça plante
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    // ✅ Modifier une commande existante
    @PutMapping("/{id}")
    public Commande modifierCommande(@PathVariable Long id, @RequestBody CommandeDTO commandeDTO) {
        return commandeService.modifierCommande(id, commandeDTO);
    }
    @GetMapping("/verifier-date")
    public boolean verifierDisponibiliteDate(@RequestParam String date) {
        return commandeService.existeCommandeLe(date);
    }
    @PutMapping("/{id}/corbeille")
    public ResponseEntity<?> mettreEnCorbeille(@PathVariable Long id) {
        commandeService.mettreEnCorbeille(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/restaurer")
    public ResponseEntity<?> restaurerCommande(@PathVariable Long id) {
        commandeService.restaurerCommande(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerDefinitivement(@PathVariable Long id) {
        commandeService.supprimerCommande(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/corbeille")
    public List<CommandeDTO> getCommandesDansCorbeille() {
        return commandeService.getCommandesDansCorbeille();
    }


}
