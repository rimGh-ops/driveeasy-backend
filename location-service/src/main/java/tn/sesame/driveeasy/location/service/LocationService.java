package tn.sesame.driveeasy.location.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.client.ClientServiceClient;
import tn.sesame.driveeasy.location.client.VehiculeClient;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.exception.BusinessRuleException;
import tn.sesame.driveeasy.location.exception.ResourceNotFoundException;
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
            // Fallback en cas d'indisponibilité temporaire
            client = new ClientDTO();
            client.setId(location.getClientId());
            client.setNom("Client #" + location.getClientId());
        }

        try {
            vehicule = vehiculeClient.getVehiculeById(location.getVehiculeId());
        } catch (Exception e) {
            vehicule = new VehiculeDTO();
            vehicule.setId(location.getVehiculeId());
            vehicule.setMarque("Véhicule #" + location.getVehiculeId());
        }

        return new LocationDetailDTO(location, client, vehicule);
    }

    public SimulationDTO.Response simulatePrice(SimulationDTO.Request req) {
        validateDates(req.getDateDebut(), req.getDateFinPrevue());

        ClientDTO client = clientServiceClient.getClientById(req.getClientId());
        VehiculeDTO vehicule = vehiculeClient.getVehiculeById(req.getVehiculeId());

        long days = ChronoUnit.DAYS.between(req.getDateDebut(), req.getDateFinPrevue());
        if (days <= 0) days = 1;

        BigDecimal dailyRate = vehicule.getPrixJournalier();
        BigDecimal gross = dailyRate.multiply(BigDecimal.valueOf(days));

        BigDecimal discountRate = getDiscountRate(client.getCategorieClient());
        BigDecimal discountAmount = gross.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        return new SimulationDTO.Response(days, dailyRate, client.getCategorieClient(),
                discountRate, gross, discountAmount, netAmount);
    }

    public Location createReservation(LocationRequestDTO req) {
        validateDates(req.getDateDebut(), req.getDateFinPrevue());

        // 1. Contrôle de l'éligibilité du client (Appel inter-service)
        EligibiliteDTO eligibilite = clientServiceClient.checkEligibilite(req.getClientId());
        if (!eligibilite.isEligible()) {
            throw new BusinessRuleException("Réservation impossible : " + eligibilite.getMotif());
        }

        ClientDTO client = clientServiceClient.getClientById(req.getClientId());

        // 2. Contrôle du véhicule et de son statut (Appel inter-service)
        VehiculeDTO vehicule = vehiculeClient.getVehiculeById(req.getVehiculeId());
        if ("MAINTENANCE".equalsIgnoreCase(vehicule.getStatut())) {
            throw new BusinessRuleException("Véhicule indisponible : le véhicule est actuellement en maintenance.");
        }

        // 3. Vérification de chevauchement sur la période demandée
        boolean conflict = locationRepository.findByVehiculeId(req.getVehiculeId()).stream()
                .filter(l -> l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS)
                .anyMatch(l -> datesOverlap(req.getDateDebut(), req.getDateFinPrevue(), l.getDateDebut(), l.getDateFinPrevue()));

        if (conflict) {
            throw new BusinessRuleException("Le véhicule est déjà réservé ou loué pour la période sélectionnée.");
        }

        // 4. Calcul du prix
        long days = ChronoUnit.DAYS.between(req.getDateDebut(), req.getDateFinPrevue());
        if (days <= 0) days = 1;

        BigDecimal dailyRate = vehicule.getPrixJournalier();
        BigDecimal gross = dailyRate.multiply(BigDecimal.valueOf(days));
        BigDecimal discountRate = getDiscountRate(client.getCategorieClient());
        BigDecimal discountAmount = gross.multiply(discountRate);
        BigDecimal total = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

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

        return locationRepository.save(loc);
    }

    public Location startLocation(Long id) {
        Location loc = getLocationById(id);
        if (loc.getStatut() != StatutLocation.RESERVEE) {
            throw new BusinessRuleException("Impossible de démarrer la location : statut actuel est " + loc.getStatut());
        }

        try {
            // Appel inter-service : passage du véhicule à LOUE
            vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "LOUE"));
        } catch (Exception e) {
            throw new BusinessRuleException("Erreur de communication avec vehicule-service lors du départ: " + e.getMessage());
        }

        loc.setStatut(StatutLocation.EN_COURS);
        return locationRepository.save(loc);
    }

    public Location returnLocation(Long id, Integer kmRetour) {
        Location loc = getLocationById(id);
        if (loc.getStatut() != StatutLocation.EN_COURS) {
            throw new BusinessRuleException("Impossible de clôturer une location qui n'est pas en cours (statut: " + loc.getStatut() + ")");
        }

        if (kmRetour != null && kmRetour < loc.getKmDepart()) {
            throw new BusinessRuleException("Le kilométrage de retour (" + kmRetour + ") ne peut pas être inférieur au départ (" + loc.getKmDepart() + ")");
        }

        loc.setKmRetour(kmRetour);
        loc.setDateRetourEffectif(LocalDate.now());

        // Calcul des pénalités de retard : 1.5 * prixJournalier par jour de retard
        BigDecimal penalite = BigDecimal.ZERO;
        if (loc.getDateRetourEffectif().isAfter(loc.getDateFinPrevue())) {
            long joursRetard = ChronoUnit.DAYS.between(loc.getDateFinPrevue(), loc.getDateRetourEffectif());
            if (joursRetard > 0) {
                BigDecimal facteurPenalite = new BigDecimal("1.5");
                penalite = loc.getPrixJournalierApplique()
                        .multiply(facteurPenalite)
                        .multiply(BigDecimal.valueOf(joursRetard))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        loc.setPenalite(penalite);
        loc.setMontantTotal(loc.getMontantTotal().add(penalite));
        loc.setStatut(StatutLocation.TERMINEE);

        // Appel inter-service : passage du véhicule à DISPONIBLE
        try {
            vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
        } catch (Exception e) {
            // Log warning mais préservation de la cohérence
        }

        // Sauvegarde de la location
        Location saved = locationRepository.save(loc);

        // Mise à jour de fidélité du client : FIDELE après 5 locations terminées, VIP après 15
        checkAndUpdateClientFidelity(loc.getClientId());

        return saved;
    }

    public Location cancelLocation(Long id) {
        Location loc = getLocationById(id);
        if (loc.getStatut() == StatutLocation.TERMINEE) {
            throw new BusinessRuleException("Impossible d'annuler une location déjà terminée.");
        }

        boolean wasStarted = (loc.getStatut() == StatutLocation.EN_COURS);
        loc.setStatut(StatutLocation.ANNULEE);

        if (wasStarted) {
            try {
                vehiculeClient.updateStatut(loc.getVehiculeId(), Map.of("statut", "DISPONIBLE"));
            } catch (Exception ignored) {
            }
        }

        return locationRepository.save(loc);
    }

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

        // Calcul du taux d'occupation : véhicules loués / parc total
        int totalVehicules = 6;
        try {
            List<VehiculeDTO> vehicules = vehiculeClient.getAllVehicules();
            if (vehicules != null && !vehicules.isEmpty()) {
                totalVehicules = vehicules.size();
            }
        } catch (Exception ignored) {
        }

        double tauxOccupation = totalVehicules > 0 ? ((double) enCours / totalVehicules) * 100.0 : 0.0;
        tauxOccupation = Math.round(tauxOccupation * 100.0) / 100.0;

        // Top véhicules les plus loués
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
            } catch (Exception ignored) {
            }

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

    private void checkAndUpdateClientFidelity(Long clientId) {
        long finishedCount = locationRepository.findByClientId(clientId).stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE)
                .count();

        try {
            if (finishedCount >= 15) {
                clientServiceClient.updateCategorie(clientId, Map.of("categorie", "VIP"));
            } else if (finishedCount >= 5) {
                clientServiceClient.updateCategorie(clientId, Map.of("categorie", "FIDELE"));
            }
        } catch (Exception ignored) {
        }
    }

    private BigDecimal getDiscountRate(String categorie) {
        if (categorie == null) return BigDecimal.ZERO;
        return switch (categorie.toUpperCase()) {
            case "VIP" -> new BigDecimal("0.10");
            case "FIDELE" -> new BigDecimal("0.05");
            default -> BigDecimal.ZERO;
        };
    }

    private void validateDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) {
            throw new BusinessRuleException("Les dates de début et de fin sont obligatoires.");
        }
        if (fin.isBefore(debut)) {
            throw new BusinessRuleException("La date de fin ne peut pas être antérieure à la date de début.");
        }
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}
