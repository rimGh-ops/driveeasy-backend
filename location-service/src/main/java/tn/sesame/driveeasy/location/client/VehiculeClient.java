package tn.sesame.driveeasy.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.VehiculeDTO;

import java.util.List;
import java.util.Map;

@FeignClient(name = "vehicule-service")
public interface VehiculeClient {

    @GetMapping("/vehicules/{id}")
    VehiculeDTO getVehiculeById(@PathVariable("id") Long id);

    @GetMapping("/vehicules")
    List<VehiculeDTO> getAllVehicules();

    @PatchMapping("/vehicules/{id}/statut")
    VehiculeDTO updateStatut(@PathVariable("id") Long id, @RequestBody Map<String, String> body);
}
