package tn.sesame.driveeasy.location.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Location {

    private Long id;

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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateRetourEffectif;

    private int kmDepart;
    private Integer kmRetour;

    private BigDecimal prixJournalierApplique;
    private BigDecimal montantTotal;
    private BigDecimal penalite = BigDecimal.ZERO;

    private StatutLocation statut = StatutLocation.RESERVEE;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Location() {
    }

    public Location(Long id, Long clientId, Long vehiculeId, LocalDate dateDebut,
                    LocalDate dateFinPrevue, LocalDate dateRetourEffectif, int kmDepart,
                    Integer kmRetour, BigDecimal prixJournalierApplique, BigDecimal montantTotal,
                    BigDecimal penalite, StatutLocation statut, LocalDateTime dateCreation) {
        this.id = id;
        this.clientId = clientId;
        this.vehiculeId = vehiculeId;
        this.dateDebut = dateDebut;
        this.dateFinPrevue = dateFinPrevue;
        this.dateRetourEffectif = dateRetourEffectif;
        this.kmDepart = kmDepart;
        this.kmRetour = kmRetour;
        this.prixJournalierApplique = prixJournalierApplique;
        this.montantTotal = montantTotal;
        this.penalite = (penalite != null) ? penalite : BigDecimal.ZERO;
        this.statut = (statut != null) ? statut : StatutLocation.RESERVEE;
        this.dateCreation = (dateCreation != null) ? dateCreation : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getDateRetourEffectif() {
        return dateRetourEffectif;
    }

    public void setDateRetourEffectif(LocalDate dateRetourEffectif) {
        this.dateRetourEffectif = dateRetourEffectif;
    }

    public int getKmDepart() {
        return kmDepart;
    }

    public void setKmDepart(int kmDepart) {
        this.kmDepart = kmDepart;
    }

    public Integer getKmRetour() {
        return kmRetour;
    }

    public void setKmRetour(Integer kmRetour) {
        this.kmRetour = kmRetour;
    }

    public BigDecimal getPrixJournalierApplique() {
        return prixJournalierApplique;
    }

    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) {
        this.prixJournalierApplique = prixJournalierApplique;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getPenalite() {
        return penalite;
    }

    public void setPenalite(BigDecimal penalite) {
        this.penalite = penalite;
    }

    public StatutLocation getStatut() {
        return statut;
    }

    public void setStatut(StatutLocation statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
