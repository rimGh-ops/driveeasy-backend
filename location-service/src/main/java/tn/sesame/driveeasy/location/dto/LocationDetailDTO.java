package tn.sesame.driveeasy.location.dto;

import tn.sesame.driveeasy.location.model.Location;

public class LocationDetailDTO {
    private Location location;
    private ClientDTO client;
    private VehiculeDTO vehicule;

    public LocationDetailDTO() {
    }

    public LocationDetailDTO(Location location, ClientDTO client, VehiculeDTO vehicule) {
        this.location = location;
        this.client = client;
        this.vehicule = vehicule;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    public VehiculeDTO getVehicule() {
        return vehicule;
    }

    public void setVehicule(VehiculeDTO vehicule) {
        this.vehicule = vehicule;
    }
}
