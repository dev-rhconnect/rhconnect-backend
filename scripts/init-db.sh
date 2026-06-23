#!/bin/bash
# Script d'initialisation de la base de données — à exécuter UNE SEULE FOIS sur un nouveau serveur
# Usage : ./scripts/init-db.sh
docker exec -i rhconnect-postgres psql -U rhconnect_user -d rhconnect_db < src/main/resources/data.sql
docker exec -i rhconnect-postgres psql -U rhconnect_user -d rhconnect_db < src/main/resources/maquette_init.sql
