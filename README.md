# RHConnect — Backend

Plateforme SaaS de Gestion des Ressources Humaines — ISM Dakar  
Stack : Java 17 · Spring Boot 3.5.14 · MySQL 8 · JWT

---

## Prérequis

- Java 17
- Maven 3.8+
- MySQL 8
- VS Code + Extension Pack for Java

---

## Installation

### 1. Cloner le projet
```bash
git clone https://github.com/rhconnect-ism/rhconnect-backend.git
cd rhconnect-backend
```

### 2. Configurer la base de données
```sql
CREATE DATABASE rhconnect;
```

### 3. Configurer application.properties
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Puis modifie les valeurs dans `application.properties` avec tes identifiants MySQL.

### 4. Lancer le projet
```bash
./mvnw spring-boot:run
```

L'API est accessible sur : `http://localhost:8080`

---

## Structure du projet

```
src/main/java/com/ism/rhconnect/
├── config/         # Configuration Spring Security, CORS
├── security/       # JWT Filter, UserDetailsService
├── entity/         # Entités JPA
├── repository/     # Interfaces Spring Data JPA
├── service/        # Logique métier
├── controller/     # Endpoints REST
├── dto/            # Objets de transfert (Request/Response)
├── exception/      # Gestion des erreurs
└── util/           # Utilitaires
```

---

## Branches

| Membre | Branche |
|---|---|
| Marie DIAGNE | sprint1/feature/auth |
| Mame Coumba SALL | sprint1/feature/vacataires |
| Merveille Joy AKODEKOU | sprint1/feature/interventions |
| Mouhamad Al Amin FALL | sprint1/feature/releves |
| Ndeye Fatou GUEYE | sprint1/feature/paie |
| Diaynaba SOW | sprint1/feature/dashboard |

---

## Équipe

Licence 3 GLRS & CDSD · ISM Dakar · 2025–2026  
Encadreur : M. Birane B. WANE
