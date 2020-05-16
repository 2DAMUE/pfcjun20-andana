package uem.dam.seg.whereipark.SharedPreferences.model;

/**
 * Clase que representa nuestro modelo, compuesto por: longitud, latitud y notas.
 * Contiene contructores para poser ser utilizada desde otras clases y
 * contiene los getters y setters de sus atributos.
 */
public class UbicationModel {

    private double latitude;
    private double longitude;
    private String notes;

    public UbicationModel() {

    }

    public UbicationModel(double latitude, double longitude, String notes) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.notes = notes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
