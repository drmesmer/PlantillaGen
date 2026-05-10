package com.plantillagen.db;

import com.plantillagen.model.PlantillaHeader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlantillaHeaderDAO {

    public List<PlantillaHeader> findAll() throws SQLException {
        List<PlantillaHeader> list = new ArrayList<>();
        String sql = "SELECT id, nombre, estado, created_at, updated_at FROM plantillas ORDER BY updated_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Optional<PlantillaHeader> findById(int id) throws SQLException {
        String sql = "SELECT id, nombre, estado, created_at, updated_at FROM plantillas WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return Optional.empty();
    }

    public int save(PlantillaHeader header) throws SQLException {
        String sql = "INSERT INTO plantillas (nombre, estado, created_at, updated_at) "
                   + "VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, header.getNombre());
            ps.setString(2, header.getEstado() != null ? header.getEstado() : "BORRADOR");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return -1;
    }

    public void update(int id, String nombre, String estado) throws SQLException {
        String sql = "UPDATE plantillas SET nombre = ?, estado = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, estado);
            ps.setInt(3, id);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM plantillas WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    private PlantillaHeader mapRow(ResultSet rs) throws SQLException {
        PlantillaHeader h = new PlantillaHeader(
            rs.getString("nombre"),
            rs.getString("estado")
        );
        h.setId(rs.getInt("id"));
        h.setCreatedAt(rs.getTimestamp("created_at"));
        h.setUpdatedAt(rs.getTimestamp("updated_at"));
        return h;
    }
}
