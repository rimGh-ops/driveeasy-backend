# DriveEasy - Backend Microservices Architecture

Plateforme de gestion et de location de véhicules conçue selon une architecture microservices moderne avec **Java 21**, **Spring Boot 3.3.x**, **Spring Cloud 2023**, **Netflix Eureka**, **Spring Cloud Config**, **Spring Cloud Gateway** et **Distributed Tracing (Zipkin / Micrometer)**.

---

## 1. Vue d'ensemble de l'Architecture

Le backend est constitué de 6 services autonomes :

| Service | Port | Rôle & Description | Dépendances Clés |
| :--- | :---: | :--- | :--- |
| **`service-registry`** | `8761` | Annuaire Eureka Server pour la découverte dynamique des services | `spring-cloud-starter-netflix-eureka-server` |
| **`config-server`** | `8888` | Serveur de configuration centralisé externalisé | `spring-cloud-config-server` |
| **`vehicule-service`** | `8081` | Gestion du parc automobile, statuts et disponibilités | Web, Eureka Client, Actuator, Micrometer |
| **`client-service`** | `8082` | Gestion des clients, de l'éligibilité et des catégories de fidélité | Web, Eureka Client, Actuator, Micrometer |
| **`location-service`** | `8083` | Orchestration des locations, calcul des tarifs et pénalités | OpenFeign, Web, Eureka Client, Micrometer |
| **`api-gateway`** | `8080` | Passerelle API unique, routage, Load Balancing, CORS, Filtre custom | `spring-cloud-starter-gateway`, Reactive |
| **`Zipkin (Docker)`** | `9411` | Traçage distribué des requêtes de bout en bout | Image Docker `openzipkin/zipkin` |

---

## 2. Prérequis Système

- **JDK 21** ou supérieur
- **Apache Maven 3.9+**
- **Docker & Docker Compose** (pour Zipkin)

---

## 3. Procédure de Lancement Pas à Pas

### Étape 1 : Démarrer Zipkin (Tracing distribué)
```bash
docker compose up -d
```
L'interface Zipkin est immédiatement accessible sur [http://localhost:9411](http://localhost:9411).

### Étape 2 : Ordre de démarrage strict des microservices
Pour garantir la résolution correcte des dépendances et de la configuration :

1. **Service Registry (Eureka)**
   ```bash
   cd service-registry
   mvn spring-boot:run
   ```
   *Vérifier sur [http://localhost:8761](http://localhost:8761)*

2. **Config Server**
   ```bash
   cd ../config-server
   mvn spring-boot:run
   ```
   *Vérifier sur [http://localhost:8888/vehicule-service/default](http://localhost:8888/vehicule-service/default)*

3. **Microservices Métier (dans des terminaux distincts)**
   ```bash
   cd ../vehicule-service
   mvn spring-boot:run
   ```
   ```bash
   cd ../client-service
   mvn spring-boot:run
   ```
   ```bash
   cd ../location-service
   mvn spring-boot:run
   ```

4. **API Gateway**
   ```bash
   cd ../api-gateway
   mvn spring-boot:run
   ```
   *Toutes les requêtes passent désormais par le port `8080`.*

---

## 4. Routes de l'API Gateway

Toutes les requêtes externes s'adressent à `http://localhost:8080` :

| Préfixe Externe | Service Cible | Filtre Appliqué |
| :--- | :--- | :--- |
| `/api/vehicules/**` | `lb://vehicule-service` | `StripPrefix=1` |
| `/api/clients/**` | `lb://client-service` | `StripPrefix=1` |
| `/api/locations/**` | `lb://location-service` | `StripPrefix=1` |

---

## 5. Jeux de Données Pré-chargés en RAM

Conformément au cahier des charges, les données sont stockées exclusivement en mémoire RAM (structures thread-safe `ConcurrentHashMap`) avec initialisation automatique :

### Véhicules
- `ID 1` : Volkswagen Golf 8 (2023, Essence, Auto, 120 TND/j, DISPONIBLE)
- `ID 2` : Peugeot 208 (2022, Diesel, Manuelle, 85 TND/j, DISPONIBLE)
- `ID 3` : Renault Clio 5 (2024, Hybride, Auto, 95 TND/j, LOUE)
- `ID 4` : Tesla Model 3 (2024, Électrique, Auto, 210 TND/j, DISPONIBLE)
- `ID 5` : Audi A3 (2021, Essence, Auto, 160 TND/j, MAINTENANCE)
- `ID 6` : Toyota RAV4 (2023, Hybride, Auto, 150 TND/j, DISPONIBLE)

### Clients
- `ID 1` : Mohamed Ben Salah (STANDARD, éligible, permis valide)
- `ID 2` : Sonia Trabelsi (FIDELE - 5% remise, éligible, permis valide)
- `ID 3` : Karim Jaziri (VIP - 10% remise, éligible, permis valide)
- `ID 4` : Ahmed Gharbi (Âge 19 ans - inéligible car < 21 ans)
- `ID 5` : Youssef Mejri (Permis expiré - inéligible avec alerte)

### Locations
- `ID 1` : Location terminée (Client 1, Véhicule 1)
- `ID 2` : Location en cours (Client 2, Véhicule 3)
- `ID 3` : Location réservée (Client 3, Véhicule 4)

---

## 6. Endpoints Principaux & Exemples de Test (cURL)

### Véhicules
- **Lister tous les véhicules** :
  ```bash
  curl http://localhost:8080/api/vehicules
  ```
- **Filtrer par disponibilité & carburant** :
  ```bash
  curl "http://localhost:8080/api/vehicules?carburant=HYBRIDE&statut=DISPONIBLE"
  ```
- **Consulter les véhicules disponibles pour une période** :
  ```bash
  curl "http://localhost:8080/api/vehicules/disponibles"
  ```

### Clients
- **Rechercher un client (nom, email ou CIN)** :
  ```bash
  curl "http://localhost:8080/api/clients?search=Trabelsi"
  ```
- **Vérifier l'éligibilité d'un client** :
  ```bash
  curl http://localhost:8080/api/clients/1/eligibilite
  curl http://localhost:8080/api/clients/4/eligibilite
  ```

### Locations & Orchestration
- **Simuler le coût d'une location sans la créer** :
  ```bash
  curl -X POST http://localhost:8080/api/locations/simulation \
    -H "Content-Type: application/json" \
    -d "{\"clientId\": 2, \"vehiculeId\": 1, \"dateDebut\": \"2026-11-20\", \"dateFinPrevue\": \"2026-11-25\"}"
  ```
- **Créer une réservation** :
  ```bash
  curl -X POST http://localhost:8080/api/locations \
    -H "Content-Type: application/json" \
    -d "{\"clientId\": 1, \"vehiculeId\": 2, \"dateDebut\": \"2026-11-20\", \"dateFinPrevue\": \"2026-11-23\"}"
  ```
- **Enregistrer le départ d'une location (véhicule passe à LOUE)** :
  ```bash
  curl -X PATCH http://localhost:8080/api/locations/3/depart
  ```
- **Enregistrer le retour (calcul kilométrage, pénalités et remise en disponibilité)** :
  ```bash
  curl -X PATCH http://localhost:8080/api/locations/2/retour \
    -H "Content-Type: application/json" \
    -d "{\"kmRetour\": 12450}"
  ```
- **Consulter les statistiques d'exploitation (KPIs)** :
  ```bash
  curl http://localhost:8080/api/locations/statistiques
  ```

---

## 7. Observabilité et Traces Zipkin

Chaque appel HTTP transitant par l'API Gateway génère des `Spans` et un `TraceId` unique propagé à travers les microservices (`api-gateway` -> `location-service` -> `vehicule-service` / `client-service`).
Visualisez les traces et les temps de réponse sur **http://localhost:9411**.
