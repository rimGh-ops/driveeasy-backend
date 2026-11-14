package tn.sesame.driveeasy.client.service;

import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.client.dto.EligibiliteDTO;
import tn.sesame.driveeasy.client.exception.BusinessRuleException;
import tn.sesame.driveeasy.client.exception.ResourceConflictException;
import tn.sesame.driveeasy.client.exception.ResourceNotFoundException;
import tn.sesame.driveeasy.client.model.CategorieClient;
import tn.sesame.driveeasy.client.model.Client;
import tn.sesame.driveeasy.client.repository.ClientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public List<Client> getAllClients(String search) {
        if (search == null || search.isBlank()) {
            return repository.findAll();
        }
        String query = search.toLowerCase().trim();
        return repository.findAll().stream()
                .filter(c -> (c.getNom() != null && c.getNom().toLowerCase().contains(query)) ||
                        (c.getPrenom() != null && c.getPrenom().toLowerCase().contains(query)) ||
                        (c.getCin() != null && c.getCin().toLowerCase().contains(query)) ||
                        (c.getEmail() != null && c.getEmail().toLowerCase().contains(query)))
                .collect(Collectors.toList());
    }

    public Client getClientById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'identifiant: " + id));
    }

    public Client createClient(Client client) {
        // Unicité CIN
        if (repository.findByCin(client.getCin()).isPresent()) {
            throw new ResourceConflictException("Le CIN " + client.getCin() + " est déjà enregistré.");
        }
        // Unicité Email
        if (repository.findByEmail(client.getEmail()).isPresent()) {
            throw new ResourceConflictException("L'adresse email " + client.getEmail() + " est déjà enregistrée.");
        }
        // Unicité Numéro Permis
        if (repository.findByNumeroPermis(client.getNumeroPermis()).isPresent()) {
            throw new ResourceConflictException("Le numéro de permis " + client.getNumeroPermis() + " est déjà enregistré.");
        }

        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDate.now());
        }
        if (client.getCategorieClient() == null) {
            client.setCategorieClient(CategorieClient.STANDARD);
        }

        return repository.save(client);
    }

    public Client updateClient(Long id, Client updated) {
        Client existing = getClientById(id);

        Optional<Client> cinConflict = repository.findByCin(updated.getCin());
        if (cinConflict.isPresent() && !cinConflict.get().getId().equals(id)) {
            throw new ResourceConflictException("Le CIN " + updated.getCin() + " est déjà utilisé par un autre client.");
        }

        Optional<Client> emailConflict = repository.findByEmail(updated.getEmail());
        if (emailConflict.isPresent() && !emailConflict.get().getId().equals(id)) {
            throw new ResourceConflictException("L'email " + updated.getEmail() + " est déjà utilisé par un autre client.");
        }

        Optional<Client> permisConflict = repository.findByNumeroPermis(updated.getNumeroPermis());
        if (permisConflict.isPresent() && !permisConflict.get().getId().equals(id)) {
            throw new ResourceConflictException("Le permis " + updated.getNumeroPermis() + " est déjà utilisé par un autre client.");
        }

        existing.setCin(updated.getCin());
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setDateNaissance(updated.getDateNaissance());
        existing.setEmail(updated.getEmail());
        existing.setTelephone(updated.getTelephone());
        existing.setAdresse(updated.getAdresse());
        existing.setNumeroPermis(updated.getNumeroPermis());
        existing.setDateExpirationPermis(updated.getDateExpirationPermis());
        if (updated.getCategorieClient() != null) {
            existing.setCategorieClient(updated.getCategorieClient());
        }

        return repository.save(existing);
    }

    public void deleteClient(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Client introuvable avec l'identifiant: " + id);
        }
        repository.deleteById(id);
    }

    public EligibiliteDTO checkEligibilite(Long id) {
        Client client = getClientById(id);
        int age = client.getAge();
        boolean permisValide = client.isPermisValide();

        if (age < 21) {
            return new EligibiliteDTO(id, false, age, permisValide,
                    "Client inéligible : l'âge minimum requis est de 21 ans (âge actuel : " + age + " ans).");
        }
        if (!permisValide) {
            return new EligibiliteDTO(id, false, age, permisValide,
                    "Client inéligible : le permis de conduire a expiré le " + client.getDateExpirationPermis() + ".");
        }

        return new EligibiliteDTO(id, true, age, permisValide, "Client parfaitement éligible à la location.");
    }

    public Client updateCategorie(Long id, CategorieClient nouvelleCategorie) {
        Client client = getClientById(id);
        client.setCategorieClient(nouvelleCategorie);
        return repository.save(client);
    }
}
