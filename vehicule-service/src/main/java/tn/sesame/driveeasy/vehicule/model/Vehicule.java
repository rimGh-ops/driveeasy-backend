package tn.sesame.driveeasy.vehicule.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Year;

public class Vehicule {

    private Long id;

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    @NotBlank(message = "La marque est obligatoire")
    private String marque;

    @NotBlank(message = "Le modèle est obligatoire")
    private String modele;

    @Min(value = 1990, message = "L'année doit être supérieure à 1990")
    private int annee;

    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;

    @NotNull(message = "Le type de carburant est obligatoire")
    private Carburant carburant;

    @NotNull(message = "Le type de boîte est obligatoire")
    private Boite boite;

    @Min(value = 1, message = "Le nombre de places doit être au moins de 1")
    private int nbPlaces;

    @Min(value = 0, message = "Le kilométrage doit être positif")
    private int kilometrage;

    @NotNull(message = "Le prix journalier est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix journalier doit être strictement positif")
    private BigDecimal prixJournalier;

    @NotNull(message = "Le statut est obligatoire")
    private StatutVehicule statut = StatutVehicule.DISPONIBLE;

    private String imageUrl;

    public Vehicule() {
    }

    public Vehicule(Long id, String immatriculation, String marque, String modele, int annee,
                    String couleur, Carburant carburant, Boite boite, int nbPlaces,
                    int kilometrage, BigDecimal prixJournalier, StatutVehicule statut, String imageUrl) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.couleur = couleur;
        this.carburant = carburant;
        this.boite = boite;
        this.nbPlaces = nbPlaces;
        this.kilometrage = kilometrage;
        this.prixJournalier = prixJournalier;
        this.statut = statut;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public Carburant getCarburant() {
        return carburant;
    }

    public void setCarburant(Carburant carburant) {
        this.carburant = carburant;
    }

    public Boite getBoite() {
        return boite;
    }

    public void setBoite(Boite boite) {
        this.boite = boite;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public BigDecimal getPrixJournalier() {
        return prixJournalier;
    }

    public void setPrixJournalier(BigDecimal prixJournalier) {
        this.prixJournalier = prixJournalier;
    }

    public StatutVehicule getStatut() {
        return statut;
    }

    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAnneeValide() {
        int currentYear = Year.now().getValue();
        return this.annee <= currentYear;
    }
}
