package tn.sesame.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SimulationDTO {

    public static class Request {
        @NotNull(message = "L'identifiant du client est obligatoire")
        private Long clientId;

        @NotNull(message = "L'identifiant du véhicule est obligatoire")
        private Long vehiculeId;

        @NotNull(message = "La date de début est obligatoire")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDebut;

        @NotNull(message = "La date de fin prévue est obligatoire")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFinPrevue;

        public Request() {
        }

        public Long getClientId() {
            return clientId;
        }

        public void setClientId(Long clientId) {
            this.clientId = clientId;
        }

        public Long getVehiculeId() {
            return vehiculeId;
        }

        public void setVehiculeId(Long vehiculeId) {
            this.vehiculeId = vehiculeId;
        }

        public LocalDate getDateDebut() {
            return dateDebut;
        }

        public void setDateDebut(LocalDate dateDebut) {
            this.dateDebut = dateDebut;
        }

        public LocalDate getDateFinPrevue() {
            return dateFinPrevue;
        }

        public void setDateFinPrevue(LocalDate dateFinPrevue) {
            this.dateFinPrevue = dateFinPrevue;
        }
    }

    public static class Response {
        private long nombreJours;
        private BigDecimal prixJournalier;
        private String categorieClient;
        private BigDecimal tauxRemise;
        private BigDecimal montantBrut;
        private BigDecimal montantRemise;
        private BigDecimal montantTotalEstime;

        public Response() {
        }

        public Response(long nombreJours, BigDecimal prixJournalier, String categorieClient,
                        BigDecimal tauxRemise, BigDecimal montantBrut, BigDecimal montantRemise,
                        BigDecimal montantTotalEstime) {
            this.nombreJours = nombreJours;
            this.prixJournalier = prixJournalier;
            this.categorieClient = categorieClient;
            this.tauxRemise = tauxRemise;
            this.montantBrut = montantBrut;
            this.montantRemise = montantRemise;
            this.montantTotalEstime = montantTotalEstime;
        }

        public long getNombreJours() {
            return nombreJours;
        }

        public void setNombreJours(long nombreJours) {
            this.nombreJours = nombreJours;
        }

        public BigDecimal getPrixJournalier() {
            return prixJournalier;
        }

        public void setPrixJournalier(BigDecimal prixJournalier) {
            this.prixJournalier = prixJournalier;
        }

        public String getCategorieClient() {
            return categorieClient;
        }

        public void setCategorieClient(String categorieClient) {
            this.categorieClient = categorieClient;
        }

        public BigDecimal getTauxRemise() {
            return tauxRemise;
        }

        public void setTauxRemise(BigDecimal tauxRemise) {
            this.tauxRemise = tauxRemise;
        }

        public BigDecimal getMontantBrut() {
            return montantBrut;
        }

        public void setMontantBrut(BigDecimal montantBrut) {
            this.montantBrut = montantBrut;
        }

        public BigDecimal getMontantRemise() {
            return montantRemise;
        }

        public void setMontantRemise(BigDecimal montantRemise) {
            this.montantRemise = montantRemise;
        }

        public BigDecimal getMontantTotalEstime() {
            return montantTotalEstime;
        }

        public void setMontantTotalEstime(BigDecimal montantTotalEstime) {
            this.montantTotalEstime = montantTotalEstime;
        }
    }
}
