package tn.sesame.driveeasy.vehicule.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.vehicule.exception.BusinessRuleException;
import tn.sesame.driveeasy.vehicule.exception.ResourceConflictException;
import tn.sesame.driveeasy.vehicule.exception.ResourceNotFoundException;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.model.Vehicule;
import tn.sesame.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehiculeService {

    private final VehiculeRepository repository;

    public VehiculeService(VehiculeRepository repository) {
        this.repository = repository;
    }

    public List<Vehicule> getAllVehicules(String marque, Carburant carburant,
                                          StatutVehicule statut, BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> marque == null || marque.isBlank() ||
                        v.getMarque().toLowerCase().contains(marque.toLowerCase().trim()) ||
                        v.getModele().toLowerCase().contains(marque.toLowerCase().trim()))
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .collect(Collectors.toList());
    }

    public Vehicule getVehiculeById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant: " + id));
    }

    public Vehicule createVehicule(Vehicule vehicule) {
        validateVehiculeRules(vehicule, null);

        // Immatriculation unique
        Optional<Vehicule> existing = repository.findByImmatriculation(vehicule.getImmatriculation());
        if (existing.isPresent()) {
            throw new ResourceConflictException("L'immatriculation " + vehicule.getImmatriculation() + " existe déjà.");
        }

        if (vehicule.getStatut() == null) {
            vehicule.setStatut(StatutVehicule.DISPONIBLE);
        }

        return repository.save(vehicule);
    }

    public Vehicule updateVehicule(Long id, Vehicule updated) {
        Vehicule existing = getVehiculeById(id);
        validateVehiculeRules(updated, id);

        Optional<Vehicule> conflict = repository.findByImmatriculation(updated.getImmatriculation());
        if (conflict.isPresent() && !conflict.get().getId().equals(id)) {
            throw new ResourceConflictException("L'immatriculation " + updated.getImmatriculation() + " est déjà utilisée par un autre véhicule.");
        }

        existing.setImmatriculation(updated.getImmatriculation());
        existing.setMarque(updated.getMarque());
        existing.setModele(updated.getModele());
        existing.setAnnee(updated.getAnnee());
        existing.setCouleur(updated.getCouleur());
        existing.setCarburant(updated.getCarburant());
        existing.setBoite(updated.getBoite());
        existing.setNbPlaces(updated.getNbPlaces());
        existing.setKilometrage(updated.getKilometrage());
        existing.setPrixJournalier(updated.getPrixJournalier());
        if (updated.getStatut() != null) {
            existing.setStatut(updated.getStatut());
        }
        if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank()) {
            existing.setImageUrl(updated.getImageUrl());
        }

        return repository.save(existing);
    }

    public void deleteVehicule(Long id) {
        Vehicule v = getVehiculeById(id);
        if (v.getStatut() == StatutVehicule.LOUE) {
            throw new BusinessRuleException("Impossible de supprimer un véhicule actuellement en location (statut LOUE).");
        }
        repository.deleteById(id);
    }

    public Vehicule updateStatut(Long id, StatutVehicule nouveauStatut) {
        Vehicule v = getVehiculeById(id);
        v.setStatut(nouveauStatut);
        return repository.save(v);
    }

    public List<Vehicule> getVehiculesDisponibles(LocalDate dateDebut, LocalDate dateFin) {
        // Un véhicule disponible ne doit être ni en MAINTENANCE ni LOUE
        return repository.findAll().stream()
                .filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
                .collect(Collectors.toList());
    }

    private void validateVehiculeRules(Vehicule vehicule, Long excludeId) {
        int currentYear = Year.now().getValue();
        if (vehicule.getAnnee() > currentYear) {
            throw new BusinessRuleException("L'année du véhicule (" + vehicule.getAnnee() + ") ne peut pas dépasser l'année courante (" + currentYear + ").");
        }
        if (vehicule.getPrixJournalier() == null || vehicule.getPrixJournalier().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le prix journalier doit être strictement supérieur à 0.");
        }
    }
}
