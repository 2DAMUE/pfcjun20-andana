package uem.dam.seg.whereipark.javaBean;

import java.io.Serializable;

public class Ubication implements Serializable {

    private long id;
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private int marker;

    public Ubication(String name, String description, double latitude, double longitude) {
        id = -1;
        marker = 1;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }
 
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getMarker() {
        return marker;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMarker(int marker) {
        this.marker = marker;
    }
}
