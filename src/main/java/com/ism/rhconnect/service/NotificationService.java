package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.response.NotificationResponse;
import com.ism.rhconnect.entity.Notification;
import com.ism.rhconnect.entity.Utilisateur;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.exception.UnauthorizedException;
import com.ism.rhconnect.repository.NotificationRepository;
import com.ism.rhconnect.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> mesDernieres() {
        Utilisateur u = getUtilisateurConnecte();
        return notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(u.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long compterNonLues() {
        Utilisateur u = getUtilisateurConnecte();
        return notificationRepository.countByDestinataireIdAndLu(u.getId(), false);
    }

    @Transactional
    public void marquerLue(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + id));
        n.setLu(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void marquerToutesLues() {
        Utilisateur u = getUtilisateurConnecte();
        List<Notification> nonLues = notificationRepository.findByDestinataireIdAndLu(u.getId(), false);
        nonLues.forEach(n -> n.setLu(true));
        notificationRepository.saveAll(nonLues);
    }

    /** Méthode interne — utilisée par les autres services pour créer une notification. */
    @Transactional
    public void creer(Utilisateur destinataire, Notification.Type type, String message) {
        notificationRepository.save(Notification.builder()
                .destinataire(destinataire)
                .type(type)
                .message(message)
                .build());
    }

    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .lu(n.isLu())
                .dateEnvoi(n.getDateEnvoi())
                .build();
    }
}
