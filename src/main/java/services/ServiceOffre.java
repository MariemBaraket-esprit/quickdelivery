package services;

import models.Offre;
import Interfaces.IService;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffre implements IService<Offre> {

    private Connection cnx;

    public ServiceOffre() {
        try {
            cnx = DatabaseConnection.getConnection();
        if (cnx == null) {
            System.out.println("Erreur: Connexion à la base de données échouée !");
        } else {
            System.out.println("Connexion réussie à la base de données.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    @Override
    public void add(Offre Offre) {
        String qry = "INSERT INTO `Offre`(`poste`, `type_contrat`, `description`, `salaire`, `date_publication`, `statut`, `formulaire_candidature`) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, Offre.getPoste());
            pstm.setString(2, Offre.getType_contrat());
            pstm.setString(3, Offre.getDescription());
            pstm.setDouble(4, Offre.getSalaire());
            pstm.setDate(5, new java.sql.Date(Offre.getDate_publication().getTime()));
            pstm.setString(6, Offre.getStatut());
            pstm.setString(7, Offre.getFormulaire_candidature());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Offre> getAll() {
        List<Offre> offres = new ArrayList<>();

        String qry = "SELECT * FROM `offre`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Offre o = new Offre();
                o.setId_offre(rs.getInt("id_offre"));
                o.setPoste(rs.getString("poste"));
                o.setType_contrat(rs.getString("type_contrat"));
                o.setDescription(rs.getString("description"));
                o.setSalaire(rs.getDouble("salaire"));
                o.setDate_publication(rs.getDate("date_publication"));
                o.setStatut(rs.getString("statut"));
                o.setFormulaire_candidature(rs.getString("formulaire_candidature"));

                offres.add(o);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return offres;
    }

    public Offre getOffreById(int id) {
        String qry = "SELECT * FROM offre WHERE id_offre = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Offre o = new Offre();
                o.setId_offre(rs.getInt("id_offre"));
                o.setPoste(rs.getString("poste"));
                o.setType_contrat(rs.getString("type_contrat"));
                o.setDescription(rs.getString("description"));
                o.setSalaire(rs.getDouble("salaire"));
                o.setDate_publication(rs.getDate("date_publication"));
                o.setStatut(rs.getString("statut"));
                o.setFormulaire_candidature(rs.getString("formulaire_candidature"));
                return o;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'offre : " + e.getMessage());
        }

        return null;
    }


    @Override
    public void update(Offre offre) {
        String sql = "UPDATE offre SET poste = ?, type_contrat = ?, description = ?, salaire = ?, statut = ?, formulaire_candidature = ? WHERE id_offre = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setString(1, offre.getPoste());
            pstmt.setString(2, offre.getType_contrat());
            pstmt.setString(3, offre.getDescription());
            pstmt.setDouble(4, offre.getSalaire());
            pstmt.setString(5, offre.getStatut());
            pstmt.setString(6, offre.getFormulaire_candidature());
            pstmt.setInt(7, offre.getId_offre());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("La mise à jour a échoué, aucune ligne affectée.");
            } else {
                System.out.println("Offre mise à jour avec succès.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'offre : " + e.getMessage());
        }
    }

    @Override
    public void delete(Offre offre) {
        String sql = "DELETE FROM offre WHERE id_offre = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
            pstmt.setInt(1, offre.getId_offre());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("La suppression a échoué, aucune ligne affectée.");
            } else {
                System.out.println("Offre supprimée avec succès.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'offre : " + e.getMessage());
        }
    }

    public List<Offre> search(String mot_cle) {
        List<Offre> resultats = new ArrayList<>();
        String requete = "SELECT * FROM offre WHERE poste LIKE ? OR description LIKE ? OR type_contrat LIKE ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(requete);
            String keyword = "%" + mot_cle + "%";
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Offre o = new Offre();
                o.setId_offre(rs.getInt("id_offre"));
                o.setPoste(rs.getString("poste"));
                o.setDescription(rs.getString("description"));
                o.setType_contrat(rs.getString("type_contrat"));
                o.setSalaire(rs.getDouble("salaire"));
                o.setStatut(rs.getString("statut"));
                o.setFormulaire_candidature(rs.getString("formulaire_candidature"));
                o.setDate_publication(rs.getDate("date_publication"));
                resultats.add(o);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
        }

        return resultats;
    }
}
