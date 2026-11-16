package tn.sesame.driveeasy.location.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.client.ClientServiceClient;
import tn.sesame.driveeasy.location.client.VehiculeClient;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.exception.BusinessRuleException;
import tn.sesame.driveeasy.location.exception.ConflictException;
import tn.sesame.driveeasy.location.exception.ResourceNotFoundException;
import tn.sesame.driveeasy.location.exception.ServiceIndisponibleException;
import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;
import tn.sesame.driveeasy.location.repository.LocationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;
    private final ClientServiceClient clientServiceClient;
    private final VehiculeClient vehiculeClient;

    public LocationService(LocationRepository locationRepository,
                           ClientServiceClient clientServiceClient,
                           VehiculeClient vehiculeClient) {
        this.locationRepository = locationRepository;
        this.clientServiceClient = clientServiceClient;
        this.vehiculeClient = vehiculeClient;
    }

    // ============================================================
    // LISTE & DÉTAIL
    // ============================================================

    public List<Location> getAllLocations(StatutLocation statut, Long clientId, Long vehiculeId,
                                          LocalDate dateDebut, LocalDate dateFin) {
        return locationRepository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || clientId.equals(l.getClientId()))
                .filter(l -> vehiculeId == null || vehiculeId.equals(l.getVehiculeId()))
                .filter(l -> dateDebut == null || !l.getDateDebut().isBefore(dateDebut))
                .filter(l -> dateFin == null || !l.getDateFinPrevue().isAfter(dateFin))
                .sorted(Comparator.comparing(Location::getDateCreation).reversed())
                .collect(Collectors.toList());
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location introuvable avec l'identifiant: " + id));
    }

    public LocationDetailDTO getEnrichedLocationById(Long id) {
        Location location = getLocationById(id);
        ClientDTO client = null;
        VehiculeDTO vehicule = null;

        try {
            client = clientServiceClient.getClientById(location.getClientId());
        } catch (Exception e) {
            log.warn("[COMPENSATION] client-service indisponible pour id={} - fallback activé", location.getClientId());
            client = new ClientDTO();
            client.setId(location.getClientId());
            client.setNom("Client #" + location.getClientId() + " (service indisponible)");
        }

        try {
            vehicule = vehiculeClient.getVehiculeById(location.getVehiculeId());
        } catch (Exception e) {
            log.warn("[COMPENSATION] vehicule-service indisponible pour id={} - fallback activé", location.getVehiculeId());
            vehicule = new VehiculeDTO();
            vehicule.setId(location.getVehiculeId());
            vehicule.setMarque("Véhicule #" + location.getVehiculeId() + " (service indisponible)");
        }

        return new LocationDetailDTO(location, client, vehicule);
    }

    // ============================================================
    // SIMULATION DE DEVIS
    // ============================================================

    public SimulationDTO.Response simulatePrice(SimulationDTO.Request req) {
        validateDates(req.getDateDebut(), req.getDateFinPrevue());

        ClientDTO client;
        VehiculeDTO vehicule;

        try {
            client = clientServiceClient.getClientById(req.getClientId());
        } catch (Exception e) {
            throw new ServiceIndisponibleException("client-service inaccessible pour la simulation.");
        }

        try {
            vehicule = vehiculeClient.getVehiculeById(req.getVehiculeId());
        } catch (Exception e) {
            throw new ServiceIndisponibleException("vehicule-service inaccessible pour la simulation.");
        }

        long days = Math.max(1, ChronoUnit.DAYS.between(req.getDateDebut(), req.getDateFinPrevue()));
        BigDecimal dailyRate = vehicule.getPrixJournalier();
        BigDecimal gross = dailyRate.multiply(BigDecimal.valueOf(days));
        BigDecimal discountRate = getDiscountRate(client.getCategorieClient());
        BigDecimal discountAmount = gross.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        return new SimulationDTO.Response(days, dailyRate, client.getCategorieClient(),
                discountRate, gross, discountAmount, netAmount);
    }

    // ============================================================
    // CRÉATION DE RÉSERVATION (avec compensation)
    // ============================================================

    public Location createReservation(LocationRequestDTO req) {
        validateDates(req.getDateDebut(), req.getDateFinPrevue());

        // 1. Contrôle éligibilité client
        EligibiliteDTO eligibilite;
        try {
            eligibilite = clientServiceClient.checkEligibilite(req.getClientId());
        } catch (Exception e) {
            // Compensation : si client-service est down, on ne peut pas vérifier l'éligibilité
            log.error("[COMPENSATION] client-service indisponible lors de la création de réservation : {}", e.getMessage());
            throw new ServiceIndisponibleException("client-service indisponible. Réservation impossible sans vérification d'éligibilité.");
        }

        if (!eligibilite.isEligible()) {
            throw new BusinessRuleException("Réservation impossible : " + eligibilite.getMotif());
        }

        ClientDTO client;
        try {
            client = clientServiceClient.getClientById(req.getClientId());
        } catch (Exception e) {
            throw new ServiceIndisponibleException("client-service indisponible. Impossible de récupérer les données client.");
        }

        // 2. Contrôle véhicule
        VehiculeDTO vehicule;
        try {
            vehicule = vehiculeClient.getVehiculeById(req.getVehiculeId());
        } catch (Exception e) {
            // Compensation : si vehicule-service est down, on ne peut pas vérifier la disponibilité
            log.error("[COMPENSATION] vehicule-service indisponible lors de la création de réservation : {}", e.getMessage());
            throw new ServiceIndisponibleException("vehicule-service indisponible. Réservation impossible sans vérification du véhicule.");
        }

        if ("MAINTENANCE".equalsIgnoreCase(vehicule.getStatut())) {
            throw new BusinessRuleException("Véhicule indisponible : en maintenance.");
        }

        // 3. Vérification chevauchement de dates
        boolean conflict = locationRepository.findByVehiculeId(req.getVehiculeId()).stream()
                .filter(l -> l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS)
                .anyMatch(l -> datesOverlap(req.getDateDebut(), req.getDateFinPrevue(),
                        l.getDateDebut(), l.getDateFinPrevue()));

        if (conflict) {
            throw new ConflictException("Le véhicule est déjà réservé ou en cours de location pour la période demandée.");
        }

        // 4. Calcul du prix
        long days = Math.max(1, ChronoUnit.DAYS.between(req.getDateDebut(), req.getDateFinPrevue()));
        BigDecimal dailyRate = vehicule.getPrixJournalier();
        BigDecimal gross = dailyRate.multiply(BigDecimal.valueOf(days));
        BigDecimal discountRate = getDiscountRate(client.getCategorieClient());
        BigDecimal total = gross.subtract(gross.multiply(discountRate)).setScale(2, RoundingMode.HALF_UP);

        Location loc = new Location();
        loc.setClientId(client.getId());
        loc.setVehiculeId(vehicule.getId());
        loc.setDateDebut(req.getDateDebut());
        loc.setDateFinPrevue(req.getDateFinPrevue());
        loc.setKmDepart(vehicule.getKilometrage());
        loc.setPrixJournalierApplique(dailyRate);
        loc.setMontantTotal(total);
        loc.setPenalite(BigDecimal.ZERO);
        loc.setStatut(StatutLocation.RESERVEE);
        loc.setDateCreation(LocalDateTime.now());

        log.info("[RESERVATION] Créée : clientId={}, vehiculeId={}, du {} au {}, montant={}",
                client.getId(), vehicule.getId(), req.getDateDebut(), req.getDateFinPrevue(), total);

        return locationRepository.save(loc);
    }

    // ============================================================
    // DÉPART DE LOCATION (avec compensation complète)
    // ============================================================

    public Location startLocation(Long id) {
        Location loc = getLocationById(id);

        if (loc.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Impossible de démarrer la location : statut actuel est " + loc.getStatut() +
                    ". Seule une location RESERVEE peut être démarrée.");
        }

        // ── Étape 1 : Passer le véhicule à LOUÉ (compensation si échec)
        try {
            vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "LOUE"));
            log.info("[DEPART] Véhicule id={} passé à LOUE", loc.getVehiculeId());
        } catch (Exception e) {
            // COMPENSATION : vehicule-service down → on annule la réservation
            log.error("[COMPENSATION] vehicule-service indisponible au départ de la location id={}. Annulation.", id);
            loc.setStatut(StatutLocation.ANNULEE);
            locationRepository.save(loc);
            throw new ServiceIndisponibleException(
                    "vehicule-service indisponible. La réservation #" + id + " a été automatiquement annulée (compensation).");
        }

        // ── Étape 2 : Mettre à jour la location en RAM (compensation si échec)
        try {
            loc.setStatut(StatutLocation.EN_COURS);
            locationRepository.save(loc);
            log.info("[DEPART] Location id={} démarrée (EN_COURS)", id);
        } catch (Exception e) {
            // COMPENSATION : on remet le véhicule DISPONIBLE
            log.error("[COMPENSATION] Erreur sauvegarde location id={}. Remise du véhicule id={} à DISPONIBLE.", id, loc.getVehiculeId());
            try {
                vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
            } catch (Exception compensationError) {
                log.error("[COMPENSATION] Impossible de remettre le véhicule DISPONIBLE : {}", compensationError.getMessage());
            }
            throw new ServiceIndisponibleException("Erreur interne lors du démarrage. Compensation activée : véhicule remis à DISPONIBLE.");
        }

        return loc;
    }

    // ============================================================
    // RETOUR DE VÉHICULE
    // ============================================================

    public Location returnLocation(Long id, Integer kmRetour) {
        Location loc = getLocationById(id);

        if (loc.getStatut() != StatutLocation.EN_COURS) {
            throw new ConflictException("Impossible de clôturer une location qui n'est pas EN_COURS (statut: " + loc.getStatut() + ")");
        }

        if (kmRetour != null && kmRetour < loc.getKmDepart()) {
            throw new BusinessRuleException("Le kilométrage de retour (" + kmRetour +
                    " km) ne peut pas être inférieur au kilométrage de départ (" + loc.getKmDepart() + " km).");
        }

        loc.setKmRetour(kmRetour);
        loc.setDateRetourEffectif(LocalDate.now());

        // Calcul des pénalités de retard
        BigDecimal penalite = BigDecimal.ZERO;
        if (loc.getDateRetourEffectif().isAfter(loc.getDateFinPrevue())) {
            long joursRetard = ChronoUnit.DAYS.between(loc.getDateFinPrevue(), loc.getDateRetourEffectif());
            if (joursRetard > 0) {
                penalite = loc.getPrixJournalierApplique()
                        .multiply(new BigDecimal("1.5"))
                        .multiply(BigDecimal.valueOf(joursRetard))
                        .setScale(2, RoundingMode.HALF_UP);
                log.info("[RETOUR] Location id={} : {} jours de retard, pénalité={} TND", id, joursRetard, penalite);
            }
        }

        loc.setPenalite(penalite);
        loc.setMontantTotal(loc.getMontantTotal().add(penalite));
        loc.setStatut(StatutLocation.TERMINEE);

        // Compensation légère : si vehicule-service down, on log mais on ne bloque PAS le retour
        try {
            vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
            log.info("[RETOUR] Véhicule id={} remis à DISPONIBLE", loc.getVehiculeId());
        } catch (Exception e) {
            log.warn("[COMPENSATION LEGERE] vehicule-service indisponible au retour. Le statut sera mis à jour manuellement. Erreur: {}", e.getMessage());
            // On ne bloque pas : le retour est enregistré, l'agent corrigera manuellement si besoin
        }

        Location saved = locationRepository.save(loc);

        // Mise à jour fidélité (non bloquante : compensation légère)
        checkAndUpdateClientFidelity(loc.getClientId());

        return saved;
    }

    // ============================================================
    // ANNULATION
    // ============================================================

    public Location cancelLocation(Long id) {
        Location loc = getLocationById(id);

        if (loc.getStatut() == StatutLocation.TERMINEE) {
            throw new ConflictException("Impossible d'annuler une location déjà terminée.");
        }
        if (loc.getStatut() == StatutLocation.ANNULEE) {
            throw new ConflictException("Cette location est déjà annulée.");
        }

        boolean wasEnCours = (loc.getStatut() == StatutLocation.EN_COURS);
        loc.setStatut(StatutLocation.ANNULEE);
        locationRepository.save(loc);

        if (wasEnCours) {
            try {
                vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
                log.info("[ANNULATION] Véhicule id={} remis à DISPONIBLE suite à annulation", loc.getVehiculeId());
            } catch (Exception e) {
                log.warn("[COMPENSATION LEGERE] vehicule-service indisponible lors de l'annulation. Erreur: {}", e.getMessage());
            }
        }

        return loc;
    }

    // ============================================================
    // STATISTIQUES
    // ============================================================

    public StatistiquesDTO getStatistiques() {
        List<Location> all = locationRepository.findAll();

        BigDecimal caTotal = all.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .map(Location::getMontantTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate now = LocalDate.now();
        BigDecimal caMois = all.stream()
                .filter(l -> l.getDateCreation() != null &&
                        l.getDateCreation().getMonth() == now.getMonth() &&
                        l.getDateCreation().getYear() == now.getYear())
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .map(Location::getMontantTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long total = all.size();
        long enCours = all.stream().filter(l -> l.getStatut() == StatutLocation.EN_COURS).count();
        long reservees = all.stream().filter(l -> l.getStatut() == StatutLocation.RESERVEE).count();
        long terminees = all.stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).count();

        int totalVehicules = 6;
        try {
            List<VehiculeDTO> vehicules = vehiculeClient.getAllVehicules();
            if (vehicules != null && !vehicules.isEmpty()) {
                totalVehicules = vehicules.size();
            }
        } catch (Exception e) {
            log.warn("[STATS] vehicule-service indisponible, utilisation de la valeur par défaut ({} véhicules)", totalVehicules);
        }

        double tauxOccupation = Math.round(((double) enCours / totalVehicules) * 10000.0) / 100.0;

        Map<Long, List<Location>> byVehicule = all.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .collect(Collectors.groupingBy(Location::getVehiculeId));

        List<StatistiquesDTO.TopVehiculeStat> topVehicules = new ArrayList<>();
        byVehicule.forEach((vehiculeId, locList) -> {
            long count = locList.size();
            BigDecimal rev = locList.stream()
                    .map(Location::getMontantTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String marqueModele = "Véhicule #" + vehiculeId;
            String immat = "TN-" + vehiculeId;
            try {
                VehiculeDTO v = vehiculeClient.getVehiculeById(vehiculeId);
                if (v != null) {
                    marqueModele = v.getMarque() + " " + v.getModele();
                    immat = v.getImmatriculation();
                }
            } catch (Exception ignored) {}

            topVehicules.add(new StatistiquesDTO.TopVehiculeStat(vehiculeId, immat, marqueModele, count, rev));
        });

        topVehicules.sort(Comparator.comparing(StatistiquesDTO.TopVehiculeStat::getNombreLocations).reversed());

        StatistiquesDTO dto = new StatistiquesDTO();
        dto.setChiffreAffairesTotal(caTotal);
        dto.setChiffreAffairesMois(caMois);
        dto.setTotalLocations(total);
        dto.setLocationsEnCours(enCours);
        dto.setLocationsReservees(reservees);
        dto.setLocationsTerminees(terminees);
        dto.setTauxOccupationParc(tauxOccupation);
        dto.setTopVehicules(topVehicules);

        return dto;
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    private void checkAndUpdateClientFidelity(Long clientId) {
        long finishedCount = locationRepository.findByClientId(clientId).stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE)
                .count();
        try {
            if (finishedCount >= 15) {
                clientServiceClient.updateCategorie(clientId, Map.of("categorie", "VIP"));
                log.info("[FIDELITE] Client id={} promu au rang VIP ({}  locations terminées)", clientId, finishedCount);
            } else if (finishedCount >= 5) {
                clientServiceClient.updateCategorie(clientId, Map.of("categorie", "FIDELE"));
                log.info("[FIDELITE] Client id={} promu au rang FIDELE ({} locations terminées)", clientId, finishedCount);
            }
        } catch (Exception e) {
            // Compensation légère : non bloquante
            log.warn("[COMPENSATION LEGERE] Mise à jour fidélité impossible pour client id={}. Sera retraitée au prochain retour.", clientId);
        }
    }

    private BigDecimal getDiscountRate(String categorie) {
        if (categorie == null) return BigDecimal.ZERO;
        return switch (categorie.toUpperCase()) {
            case "VIP"    -> new BigDecimal("0.10");
            case "FIDELE" -> new BigDecimal("0.05");
            default       -> BigDecimal.ZERO;
        };
    }

    private void validateDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) {
            throw new BusinessRuleException("Les dates de début et de fin sont obligatoires.");
        }
        if (fin.isBefore(debut)) {
            throw new BusinessRuleException("La date de fin (" + fin + ") ne peut pas être antérieure à la date de début (" + debut + ").");
        }
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}
