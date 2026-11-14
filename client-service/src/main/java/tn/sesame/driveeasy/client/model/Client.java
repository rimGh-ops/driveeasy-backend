package tn.sesame.driveeasy.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;

public class Client {

    private Long id;

    @NotBlank(message = "Le CIN est obligatoire")
    private String cin;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "Le numéro de permis est obligatoire")
    private String numeroPermis;

    @NotNull(message = "La date d'expiration du permis est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateExpirationPermis;

    private CategorieClient categorieClient = CategorieClient.STANDARD;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateInscription = LocalDate.now();

    public Client() {
    }

    public Client(Long id, String cin, String nom, String prenom, LocalDate dateNaissance,
                  String email, String telephone, String adresse, String numeroPermis,
                  LocalDate dateExpirationPermis, CategorieClient categorieClient, LocalDate dateInscription) {
        this.id = id;
        this.cin = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.numeroPermis = numeroPermis;
        this.dateExpirationPermis = dateExpirationPermis;
        this.categorieClient = (categorieClient != null) ? categorieClient : CategorieClient.STANDARD;
        this.dateInscription = (dateInscription != null) ? dateInscription : LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumeroPermis() {
        return numeroPermis;
    }

    public void setNumeroPermis(String numeroPermis) {
        this.numeroPermis = numeroPermis;
    }

    public LocalDate getDateExpirationPermis() {
        return dateExpirationPermis;
    }

    public void setDateExpirationPermis(LocalDate dateExpirationPermis) {
        this.dateExpirationPermis = dateExpirationPermis;
    }

    public CategorieClient getCategorieClient() {
        return categorieClient;
    }

    public void setCategorieClient(CategorieClient categorieClient) {
        this.categorieClient = categorieClient;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public int getAge() {
        if (dateNaissance == null) return 0;
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public boolean isPermisValide() {
        if (dateExpirationPermis == null) return false;
        return !dateExpirationPermis.isBefore(LocalDate.now());
    }

    public boolean isEligible() {
        return getAge() >= 21 && isPermisValide();
    }
}
