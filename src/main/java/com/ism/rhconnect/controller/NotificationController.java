package com.ism.rhconnect.controller;

import com.ism.rhconnect.dto.response.NotificationResponse;
import com.ism.rhconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Récupérer les notifications de l'utilisateur connecté. */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> mesDernieres() {
        return ResponseEntity.ok(notificationService.mesDernieres());
    }

    /** Nombre de notifications non lues. */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> compterNonLues() {
        return ResponseEntity.ok(Map.of("nonLues", notificationService.compterNonLues()));
    }

    /** Marquer une notification comme lue. */
    @PatchMapping("/{id}/lire")
    public ResponseEntity<Void> marquerLue(@PathVariable Long id) {
        notificationService.marquerLue(id);
        return ResponseEntity.noContent().build();
    }

    /** Marquer toutes les notifications comme lues. */
    @PatchMapping("/lire-tout")
    public ResponseEntity<Void> marquerToutesLues() {
        notificationService.marquerToutesLues();
        return ResponseEntity.noContent().build();
    }
}
