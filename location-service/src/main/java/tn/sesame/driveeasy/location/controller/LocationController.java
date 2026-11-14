package tn.sesame.driveeasy.location.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;
import tn.sesame.driveeasy.location.service.LocationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(locationService.getAllLocations(statut, clientId, vehiculeId, dateDebut, dateFin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDetailDTO> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getEnrichedLocationById(id));
    }

    @PostMapping
    public ResponseEntity<Location> createReservation(@Valid @RequestBody LocationRequestDTO req) {
        Location created = locationService.createReservation(req);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/simulation")
    public ResponseEntity<SimulationDTO.Response> simulatePrice(@Valid @RequestBody SimulationDTO.Request req) {
        return ResponseEntity.ok(locationService.simulatePrice(req));
    }

    @PatchMapping("/{id}/depart")
    public ResponseEntity<Location> startLocation(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.startLocation(id));
    }

    @PatchMapping("/{id}/retour")
    public ResponseEntity<Location> returnLocation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        Integer kmRetour = null;
        if (body != null && body.containsKey("kmRetour")) {
            Object kmObj = body.get("kmRetour");
            if (kmObj instanceof Number) {
                kmRetour = ((Number) kmObj).intValue();
            } else if (kmObj instanceof String) {
                kmRetour = Integer.parseInt((String) kmObj);
            }
        }
        return ResponseEntity.ok(locationService.returnLocation(id, kmRetour));
    }

    @PatchMapping("/{id}/annulation")
    public ResponseEntity<Location> cancelLocation(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.cancelLocation(id));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesDTO> getStatistiques() {
        return ResponseEntity.ok(locationService.getStatistiques());
    }
}
