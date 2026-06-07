package com.ism.rhconnect.repository;

import com.ism.rhconnect.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUtilisateurIdOrderByDateActionDesc(Long utilisateurId);
    List<AuditLog> findByRessourceOrderByDateActionDesc(String ressource);
}
