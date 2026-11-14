package tn.sesame.driveeasy.location.dto;

import java.math.BigDecimal;
import java.util.List;

public class StatistiquesDTO {
    private BigDecimal chiffreAffairesTotal;
    private BigDecimal chiffreAffairesMois;
    private long totalLocations;
    private long locationsEnCours;
    private long locationsReservees;
    private long locationsTerminees;
    private double tauxOccupationParc; // pourcentage ex: 33.3%
    private List<TopVehiculeStat> topVehicules;

    public StatistiquesDTO() {
    }

    public static class TopVehiculeStat {
        private Long vehiculeId;
        private String immatriculation;
        private String marqueModele;
        private long nombreLocations;
        private BigDecimal revenuGenere;

        public TopVehiculeStat() {
        }

        public TopVehiculeStat(Long vehiculeId, String immatriculation, String marqueModele,
                               long nombreLocations, BigDecimal revenuGenere) {
            this.vehiculeId = vehiculeId;
            this.immatriculation = immatriculation;
            this.marqueModele = marqueModele;
            this.nombreLocations = nombreLocations;
            this.revenuGenere = revenuGenere;
        }

        public Long getVehiculeId() {
            return vehiculeId;
        }

        public void setVehiculeId(Long vehiculeId) {
            this.vehiculeId = vehiculeId;
        }

        public String getImmatriculation() {
            return immatriculation;
        }

        public void setImmatriculation(String immatriculation) {
            this.immatriculation = immatriculation;
        }

        public String getMarqueModele() {
            return marqueModele;
        }

        public void setMarqueModele(String marqueModele) {
            this.marqueModele = marqueModele;
        }

        public long getNombreLocations() {
            return nombreLocations;
        }

        public void setNombreLocations(long nombreLocations) {
            this.nombreLocations = nombreLocations;
        }

        public BigDecimal getRevenuGenere() {
            return revenuGenere;
        }

        public void setRevenuGenere(BigDecimal revenuGenere) {
            this.revenuGenere = revenuGenere;
        }
    }

    public BigDecimal getChiffreAffairesTotal() {
        return chiffreAffairesTotal;
    }

    public void setChiffreAffairesTotal(BigDecimal chiffreAffairesTotal) {
        this.chiffreAffairesTotal = chiffreAffairesTotal;
    }

    public BigDecimal getChiffreAffairesMois() {
        return chiffreAffairesMois;
    }

    public void setChiffreAffairesMois(BigDecimal chiffreAffairesMois) {
        this.chiffreAffairesMois = chiffreAffairesMois;
    }

    public long getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(long totalLocations) {
        this.totalLocations = totalLocations;
    }

    public long getLocationsEnCours() {
        return locationsEnCours;
    }

    public void setLocationsEnCours(long locationsEnCours) {
        this.locationsEnCours = locationsEnCours;
    }

    public long getLocationsReservees() {
        return locationsReservees;
    }

    public void setLocationsReservees(long locationsReservees) {
        this.locationsReservees = locationsReservees;
    }

    public long getLocationsTerminees() {
        return locationsTerminees;
    }

    public void setLocationsTerminees(long locationsTerminees) {
        this.locationsTerminees = locationsTerminees;
    }

    public double getTauxOccupationParc() {
        return tauxOccupationParc;
    }

    public void setTauxOccupationParc(double tauxOccupationParc) {
        this.tauxOccupationParc = tauxOccupationParc;
    }

    public List<TopVehiculeStat> getTopVehicules() {
        return topVehicules;
    }

    public void setTopVehicules(List<TopVehiculeStat> topVehicules) {
        this.topVehicules = topVehicules;
    }
}
