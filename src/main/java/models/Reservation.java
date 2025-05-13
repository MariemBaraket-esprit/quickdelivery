package models;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private String numero;
    private String client;
    private int id_client;
    private String vehicule;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String destination;
    private String statut;

    public Reservation(int id, String numero, String client, int id_client, String vehicule, 
                      LocalDateTime dateDebut, LocalDateTime dateFin, String destination, String statut) {
        this.id = id;
        this.numero = numero;
        this.client = client;
        this.id_client = id_client;
        this.vehicule = vehicule;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.destination = destination;
        this.statut = statut;
    }

    // Getters
    public int getId() { return id; }
    public String getNumero() { return numero; }
    public String getClient() { return client; }
    public String getVehicule() { return vehicule; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public String getDestination() { return destination; }
    public int getIdClient() { return id_client; }
    public String getStatut() { return statut; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setClient(String client) { this.client = client; }
    public void setVehicule(String vehicule) { this.vehicule = vehicule; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setIdClient(int id_client) { this.id_client = id_client; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", client='" + client + '\'' +
                ", vehicule='" + vehicule + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", destination='" + destination + '\'' +
                '}';
    }
} 