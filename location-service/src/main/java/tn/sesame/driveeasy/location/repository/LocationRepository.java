package tn.sesame.driveeasy.location.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class LocationRepository {

    private final Map<Long, Location> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @PostConstruct
    public void initData() {
        // Location 1 : Terminée le mois dernier
        save(new Location(null, 1L, 1L, LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(15),
                20000, 20750, new BigDecimal("120.00"), new BigDecimal("600.00"),
                BigDecimal.ZERO, StatutLocation.TERMINEE, LocalDateTime.now().minusDays(25)));

        // Location 2 : En cours (véhicule 3 loué par Sonia Trabelsi)
        save(new Location(null, 2L, 3L, LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(3), null,
                12000, null, new BigDecimal("95.00"), new BigDecimal("451.25"),
                BigDecimal.ZERO, StatutLocation.EN_COURS, LocalDateTime.now().minusDays(4)));

        // Location 3 : Réservée pour demain
        save(new Location(null, 3L, 4L, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5), null,
                8500, null, new BigDecimal("210.00"), new BigDecimal("756.00"),
                BigDecimal.ZERO, StatutLocation.RESERVEE, LocalDateTime.now().minusHours(12)));
    }

    public List<Location> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Location> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    public List<Location> findByClientId(Long clientId) {
        if (clientId == null) return Collections.emptyList();
        List<Location> res = new ArrayList<>();
        for (Location loc : storage.values()) {
            if (clientId.equals(loc.getClientId())) {
                res.add(loc);
            }
        }
        return res;
    }

    public List<Location> findByVehiculeId(Long vehiculeId) {
        if (vehiculeId == null) return Collections.emptyList();
        List<Location> res = new ArrayList<>();
        for (Location loc : storage.values()) {
            if (vehiculeId.equals(loc.getVehiculeId())) {
                res.add(loc);
            }
        }
        return res;
    }

    public Location save(Location location) {
        if (location.getId() == null) {
            location.setId(idSequence.incrementAndGet());
        }
        storage.put(location.getId(), location);
        return location;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
