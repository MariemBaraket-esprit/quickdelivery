package utils;

import models.Vehicule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static final int DAYS_THRESHOLD = 30; // Notifier 30 jours avant la date
    
    public static class VehicleNotification {
        private final String immatriculation;
        private final String type;
        private final LocalDate date;
        private final String message;
        
        public VehicleNotification(String immatriculation, String type, LocalDate date) {
            this.immatriculation = immatriculation;
            this.type = type;
            this.date = date;
            this.message = String.format("%s du véhicule %s arrive à échéance le %s", 
                type, immatriculation, date.toString());
        }
        
        public String getImmatriculation() { return immatriculation; }
        public String getType() { return type; }
        public LocalDate getDate() { return date; }
        public String getMessage() { return message; }
    }
    
    public static List<VehicleNotification> checkVehicleDates(List<Vehicule> vehicles) {
        List<VehicleNotification> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (Vehicule v : vehicles) {
            checkDate(v.getImmatriculation(), "Entretien", v.getDateEntretien(), today, notifications);
            checkDate(v.getImmatriculation(), "Visite technique", v.getDateVisiteTechnique(), today, notifications);
            checkDate(v.getImmatriculation(), "Vidange", v.getDateVidange(), today, notifications);
            checkDate(v.getImmatriculation(), "Assurance", v.getDateAssurance(), today, notifications);
            checkDate(v.getImmatriculation(), "Vignette", v.getDateVignette(), today, notifications);
        }
        
        return notifications;
    }
    
    private static void checkDate(String immatriculation, String type, LocalDate date, 
                                LocalDate today, List<VehicleNotification> notifications) {
        if (date != null) {
            long daysUntil = ChronoUnit.DAYS.between(today, date);
            if (daysUntil >= 0 && daysUntil <= DAYS_THRESHOLD) {
                notifications.add(new VehicleNotification(immatriculation, type, date));
            }
        }
    }
} 