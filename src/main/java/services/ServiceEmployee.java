package services;

import Interfaces.IService;
import models.Employee;
import models.Offre;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEmployee implements IService<Employee> {
    private Connection cnx;

    public ServiceEmployee() {
        try {
            cnx = DatabaseConnection.getConnection();
            if (cnx == null) {
                System.err.println("Erreur: Connexion à la base de données échouée !");
            } else {
                System.out.println("Connexion réussie à la base de données.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void add(Employee e) {
        String qry = "INSERT INTO employee (id_offre, nom, prenom, email, telephone, date_embauche, statut_emploi, salaire_actuel, cv_path) VALUES (?,?,?,?,?,?,?,?,?)";
        String updateOffreQuery = "UPDATE offre SET statut = 'cloturé' WHERE id_offre = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry);
        PreparedStatement updatePstm=cnx.prepareStatement(updateOffreQuery)) {
            pstm.setInt(1, e.getId_offre());
            pstm.setString(2, e.getNom());
            pstm.setString(3, e.getPrenom());
            pstm.setString(4, e.getEmail());
            pstm.setString(5, e.getTelephone());
            pstm.setDate(6, new java.sql.Date(e.getDate_embauche().getTime()));
            pstm.setString(7, e.getStatut_emploi());
            pstm.setDouble(8, e.getSalaire_actuel());
            pstm.setString(9, e.getCv_path());
            pstm.executeUpdate();

            updatePstm.setInt(1, e.getId_offre());
            updatePstm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Employee> getAll() {
        List<Employee> employees = new ArrayList<>();
        if (cnx == null) {
            System.err.println("Erreur: Pas de connexion à la base de données !");
            return employees;
        }

        String qry = "SELECT * FROM employee";
        try (Statement stm = cnx.createStatement(); ResultSet rs = stm.executeQuery(qry)) {
            System.out.println("Exécution de la requête : " + qry);
            while (rs.next()) {
                Employee e = new Employee();
                e.setId_Employe(rs.getInt("id_employe"));
                e.setId_offre(rs.getInt("id_offre"));
                e.setNom(rs.getString("nom"));
                e.setPrenom(rs.getString("prenom"));
                e.setEmail(rs.getString("email"));
                e.setTelephone(rs.getString("telephone"));
                e.setDate_embauche(rs.getDate("date_embauche"));
                e.setStatut_emploi(rs.getString("statut_emploi"));
                e.setSalaire_actuel(rs.getDouble("salaire_actuel"));
                e.setCv_path(rs.getString("cv_path"));
                employees.add(e);
                System.out.println("Employé trouvé : " + e.getNom() + " " + e.getPrenom());
            }
            System.out.println("Nombre total d'employés trouvés : " + employees.size());
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des employés : " + ex.getMessage());
            ex.printStackTrace();
        }
        return employees;
    }

    public void update(Employee e) {
        String qry = "UPDATE employee SET id_offre=?, nom=?, prenom=?, email=?, telephone=?, date_embauche=?, statut_emploi=?, salaire_actuel=?, cv_path=? WHERE id_employe=?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, e.getId_offre());
            pstm.setString(2, e.getNom());
            pstm.setString(3, e.getPrenom());
            pstm.setString(4, e.getEmail());
            pstm.setString(5, e.getTelephone());
            pstm.setDate(6, new java.sql.Date(e.getDate_embauche().getTime()));
            pstm.setString(7, e.getStatut_emploi());
            pstm.setDouble(8, e.getSalaire_actuel());
            pstm.setString(9, e.getCv_path());
            pstm.setInt(10, e.getId_Employe());
            pstm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void delete(Employee e) {
        String sql = "DELETE FROM employee WHERE id_employe = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, e.getId_Employe());
            ps.executeUpdate();
            System.out.println("Employé supprimé !");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la suppression : " + ex.getMessage());
        }
    }

    public List<Employee> search(String mot_cle) {
        List<Employee> resultats = new ArrayList<>();
        String requete = "SELECT * FROM employee WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(requete);
            String keyword = "%" + mot_cle + "%";
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Employee e = new Employee();
                e.setId_Employe(rs.getInt("id_employe"));
                e.setId_offre(rs.getInt("id_offre"));
                e.setNom(rs.getString("nom"));
                e.setPrenom(rs.getString("prenom"));
                e.setEmail(rs.getString("email"));
                e.setTelephone(rs.getString("telephone"));
                e.setDate_embauche(rs.getDate("date_embauche"));
                e.setStatut_emploi(rs.getString("statut_emploi"));
                e.setSalaire_actuel(rs.getDouble("salaire_actuel"));
                e.setCv_path(rs.getString("cv_path"));
                resultats.add(e);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
        }

        return resultats;
    }

}
