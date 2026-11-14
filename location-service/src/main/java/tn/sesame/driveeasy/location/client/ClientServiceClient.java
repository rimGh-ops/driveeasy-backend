package tn.sesame.driveeasy.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.ClientDTO;
import tn.sesame.driveeasy.location.dto.EligibiliteDTO;

import java.util.Map;

@FeignClient(name = "client-service")
public interface ClientServiceClient {

    @GetMapping("/clients/{id}")
    ClientDTO getClientById(@PathVariable("id") Long id);

    @GetMapping("/clients/{id}/eligibilite")
    EligibiliteDTO checkEligibilite(@PathVariable("id") Long id);

    @PatchMapping("/clients/{id}/categorie")
    ClientDTO updateCategorie(@PathVariable("id") Long id, @RequestBody Map<String, String> body);
}
