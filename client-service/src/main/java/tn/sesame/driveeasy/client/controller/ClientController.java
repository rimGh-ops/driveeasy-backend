package tn.sesame.driveeasy.client.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.client.dto.EligibiliteDTO;
import tn.sesame.driveeasy.client.model.CategorieClient;
import tn.sesame.driveeasy.client.model.Client;
import tn.sesame.driveeasy.client.service.ClientService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(clientService.getAllClients(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@Valid @RequestBody Client client) {
        Client created = clientService.createClient(client);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @Valid @RequestBody Client client) {
        return ResponseEntity.ok(clientService.updateClient(id, client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/eligibilite")
    public ResponseEntity<EligibiliteDTO> checkEligibilite(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.checkEligibilite(id));
    }

    @PatchMapping("/{id}/categorie")
    public ResponseEntity<Client> updateCategorie(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String catStr = body.get("categorie");
        if (catStr == null) {
            return ResponseEntity.badRequest().build();
        }
        CategorieClient categorie = CategorieClient.valueOf(catStr.toUpperCase());
        return ResponseEntity.ok(clientService.updateCategorie(id, categorie));
    }
}
