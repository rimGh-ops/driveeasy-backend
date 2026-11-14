package tn.sesame.driveeasy.client.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.client.model.CategorieClient;
import tn.sesame.driveeasy.client.model.Client;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ClientRepository {

    private final Map<Long, Client> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @PostConstruct
    public void initData() {
        // Client standard éligible
        save(new Client(null, "08123456", "Ben Salah", "Mohamed",
                LocalDate.of(1996, 4, 15), "mohamed.bensalah@email.com", "+216 22 123 456",
                "12 Rue des Orangers, Tunis", "TN-1996-08123",
                LocalDate.now().plusYears(4), CategorieClient.STANDARD, LocalDate.now().minusMonths(6)));

        // Client fidèle éligible
        save(new Client(null, "09234567", "Trabelsi", "Sonia",
                LocalDate.of(1990, 8, 22), "sonia.trabelsi@email.com", "+216 98 456 789",
                "45 Avenue Habib Bourguiba, Sousse", "TN-1990-09234",
                LocalDate.now().plusYears(2), CategorieClient.FIDELE, LocalDate.now().minusYears(1)));

        // Client VIP éligible
        save(new Client(null, "07345678", "Jaziri", "Karim",
                LocalDate.of(1982, 11, 5), "karim.jaziri@email.com", "+216 55 987 654",
                "8 Impasse du Lac, Berges du Lac 2, Tunis", "TN-1982-07345",
                LocalDate.now().plusYears(5), CategorieClient.VIP, LocalDate.now().minusYears(2)));

        // Client inéligible (âge < 21)
        save(new Client(null, "14456789", "Gharbi", "Ahmed",
                LocalDate.of(2007, 3, 10), "ahmed.gharbi@email.com", "+216 29 111 222",
                "3 Rue de la Liberté, Ariana", "TN-2007-14456",
                LocalDate.now().plusYears(3), CategorieClient.STANDARD, LocalDate.now().minusMonths(1)));

        // Client inéligible (permis expiré)
        save(new Client(null, "06567890", "Mejri", "Youssef",
                LocalDate.of(1994, 1, 18), "youssef.mejri@email.com", "+216 20 333 444",
                "77 Boulevard de l'Environnement, Nabeul", "TN-1994-06567",
                LocalDate.now().minusMonths(2), CategorieClient.STANDARD, LocalDate.now().minusMonths(8)));
    }

    public List<Client> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Client> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Client> findByCin(String cin) {
        if (cin == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> c.getCin() != null && c.getCin().equalsIgnoreCase(cin.trim()))
                .findFirst();
    }

    public Optional<Client> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public Optional<Client> findByNumeroPermis(String numeroPermis) {
        if (numeroPermis == null) return Optional.empty();
        return storage.values().stream()
                .filter(c -> c.getNumeroPermis() != null && c.getNumeroPermis().equalsIgnoreCase(numeroPermis.trim()))
                .findFirst();
    }

    public Client save(Client client) {
        if (client.getId() == null) {
            client.setId(idSequence.incrementAndGet());
        }
        storage.put(client.getId(), client);
        return client;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
