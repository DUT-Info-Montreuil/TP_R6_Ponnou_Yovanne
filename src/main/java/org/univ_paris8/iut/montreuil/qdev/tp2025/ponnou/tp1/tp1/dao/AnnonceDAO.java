package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.db.ConnectionDB;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnonceDAO extends DAO<Annonce> {

    private Connection conn() throws ClassNotFoundException, SQLException {
        return ConnectionDB.getInstance();
    }

    @Override
    public Annonce find(Long id) throws Exception {
        String sql = "SELECT id, title, description, adress, mail, date FROM annonce WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Annonce a = new Annonce();
                a.setId(rs.getLong("id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setAdress(rs.getString("adress"));
                a.setMail(rs.getString("mail"));
                a.setDate(rs.getTimestamp("date"));
                return a;
            }
        }
    }

    @Override
    public List<Annonce> findAll() throws Exception {
        String sql = "SELECT id, title, description, adress, mail, date FROM annonce ORDER BY date DESC";
        List<Annonce> out = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Annonce a = new Annonce();
                a.setId(rs.getLong("id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setAdress(rs.getString("adress"));
                a.setMail(rs.getString("mail"));
                a.setDate(rs.getTimestamp("date"));
                out.add(a);
            }
        }
        return out;
    }

    @Override
    public boolean create(Annonce obj) throws Exception {
        String sql = "INSERT INTO annonce(title, description, adress, mail) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getAdress());
            ps.setString(4, obj.getMail());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean update(Annonce obj) throws Exception {
        String sql = "UPDATE annonce SET title=?, description=?, adress=?, mail=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getAdress());
            ps.setString(4, obj.getMail());
            ps.setLong(5, obj.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(Long id) throws Exception {
        String sql = "DELETE FROM annonce WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
