package com.catering.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfilController {

    // ✅ Utilise un chemin absolu propre au projet (ex: racine du projet backend)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    @PostMapping("/uploadHeader")
    public ResponseEntity<String> uploadHeader(@RequestParam("file") MultipartFile file) {
        return enregistrerFichier(file, "header.jpg");
    }

    @PostMapping("/uploadFooter")
    public ResponseEntity<String> uploadFooter(@RequestParam("file") MultipartFile file) {
        return enregistrerFichier(file, "footer.jpg");
    }

    private ResponseEntity<String> enregistrerFichier(MultipartFile file, String nomFichier) {
        try {
            File dossier = new File(UPLOAD_DIR);
            if (!dossier.exists()) {
                dossier.mkdirs(); // ✅ Crée le dossier s'il n'existe pas
            }

            File dest = new File(Paths.get(UPLOAD_DIR, nomFichier).toString());
            file.transferTo(dest); // ✅ Enregistre l'image

            return ResponseEntity.ok("✅ Fichier '" + nomFichier + "' uploadé avec succès.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("❌ Échec de l'upload du fichier : " + e.getMessage());
        }
    }
}
