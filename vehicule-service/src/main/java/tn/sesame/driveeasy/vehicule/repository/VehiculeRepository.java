package tn.sesame.driveeasy.vehicule.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.vehicule.model.Boite;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.model.Vehicule;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class VehiculeRepository {

    private final Map<Long, Vehicule> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @PostConstruct
    public void initData() {
        save(new Vehicule(null, "210-TN-4521", "Volkswagen", "Golf 8", 2023, "Gris",
                Carburant.ESSENCE, Boite.AUTOMATIQUE, 5, 24000, new BigDecimal("120.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800"));

        save(new Vehicule(null, "198-TN-8832", "Peugeot", "208", 2022, "Bleu",
                Carburant.DIESEL, Boite.MANUELLE, 5, 45000, new BigDecimal("85.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800"));

        save(new Vehicule(null, "225-TN-1144", "Renault", "Clio 5", 2024, "Blanc",
                Carburant.HYBRIDE, Boite.AUTOMATIQUE, 5, 12000, new BigDecimal("95.00"),
                StatutVehicule.LOUE, "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800"));

        save(new Vehicule(null, "230-TN-9901", "Tesla", "Model 3", 2024, "Noir",
                Carburant.ELECTRIQUE, Boite.AUTOMATIQUE, 5, 8500, new BigDecimal("210.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800"));

        save(new Vehicule(null, "185-TN-3409", "Audi", "A3 Sportback", 2021, "Gris Nardo",
                Carburant.ESSENCE, Boite.AUTOMATIQUE, 5, 62000, new BigDecimal("160.00"),
                StatutVehicule.MAINTENANCE, "https://images.unsplash.com/photo-1603584173870-7f23fdae1b7a?w=800"));

        save(new Vehicule(null, "215-TN-6750", "Toyota", "RAV4", 2023, "Argent",
                Carburant.HYBRIDE, Boite.AUTOMATIQUE, 5, 31000, new BigDecimal("150.00"),
                StatutVehicule.DISPONIBLE, "https://images.unsplash.com/photo-1581540222194-0def2dda95b8?w=800"));
    }

    public List<Vehicule> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Vehicule> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        if (immatriculation == null) return Optional.empty();
        return storage.values().stream()
                .filter(v -> v.getImmatriculation() != null &&
                        v.getImmatriculation().equalsIgnoreCase(immatriculation.trim()))
                .findFirst();
    }

    public Vehicule save(Vehicule vehicule) {
        if (vehicule.getId() == null) {
            vehicule.setId(idSequence.incrementAndGet());
        }
        storage.put(vehicule.getId(), vehicule);
        return vehicule;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
