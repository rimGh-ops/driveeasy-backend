package tn.sesame.driveeasy.location.dto;

public class EligibiliteDTO {
    private Long clientId;
    private boolean eligible;
    private int age;
    private boolean permisValide;
    private String motif;

    public EligibiliteDTO() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isPermisValide() {
        return permisValide;
    }

    public void setPermisValide(boolean permisValide) {
        this.permisValide = permisValide;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
