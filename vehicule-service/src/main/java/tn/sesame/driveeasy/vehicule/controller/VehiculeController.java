package tn.sesame.driveeasy.vehicule.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.vehicule.model.Carburant;
import tn.sesame.driveeasy.vehicule.model.StatutVehicule;
import tn.sesame.driveeasy.vehicule.model.Vehicule;
import tn.sesame.driveeasy.vehicule.service.VehiculeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    @GetMapping
    public ResponseEntity<List<Vehicule>> getAllVehicules(
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) Carburant carburant,
            @RequestParam(required = false) StatutVehicule statut,
            @RequestParam(required = false) BigDecimal prixMax) {
        return ResponseEntity.ok(vehiculeService.getAllVehicules(marque, carburant, statut, prixMax));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicule> getVehiculeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
    }

    @PostMapping
    public ResponseEntity<Vehicule> createVehicule(@Valid @RequestBody Vehicule vehicule) {
        Vehicule created = vehiculeService.createVehicule(vehicule);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicule> updateVehicule(@PathVariable Long id, @Valid @RequestBody Vehicule vehicule) {
        return ResponseEntity.ok(vehiculeService.updateVehicule(id, vehicule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Vehicule> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statutStr = body.get("statut");
        if (statutStr == null) {
            return ResponseEntity.badRequest().build();
        }
        StatutVehicule statut = StatutVehicule.valueOf(statutStr.toUpperCase());
        return ResponseEntity.ok(vehiculeService.updateStatut(id, statut));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Vehicule>> getVehiculesDisponibles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(vehiculeService.getVehiculesDisponibles(dateDebut, dateFin));
    }
}
