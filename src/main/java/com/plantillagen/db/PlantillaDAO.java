package com.plantillagen.db;

import com.plantillagen.model.PlantillaEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PlantillaDAO {

    public List<PlantillaEntry> findByLinea(int lineaId) throws SQLException {
        List<PlantillaEntry> list = new ArrayList<>();
        String sql = "SELECT p.id, p.linea_id, p.operario_id, p.es_lider, p.tiene_formacion, p.orden, "
                   + "o.codigo AS operario_codigo, o.nombre AS operario_nombre "
                   + "FROM plantilla p "
                   + "JOIN operarios o ON o.id = p.operario_id "
                   + "WHERE p.linea_id = ? "
                   + "ORDER BY p.orden";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, lineaId);
            rs = ps.executeQuery();
            while (rs.next()) {
                PlantillaEntry entry = mapRow(rs);
                list.add(entry);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<PlantillaEntry> findAll() throws SQLException {
        List<PlantillaEntry> list = new ArrayList<>();
        String sql = "SELECT p.id, p.linea_id, p.operario_id, p.es_lider, p.tiene_formacion, p.orden, "
                   + "o.codigo AS operario_codigo, o.nombre AS operario_nombre "
                   + "FROM plantilla p "
                   + "JOIN operarios o ON o.id = p.operario_id "
                   + "ORDER BY p.linea_id, p.orden";
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

    public int save(PlantillaEntry entry) throws SQLException {
        String sql = "INSERT INTO plantilla (linea_id, operario_id, es_lider, tiene_formacion, orden, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) "
                   + "ON CONFLICT (linea_id, operario_id) DO UPDATE SET "
                   + "es_lider = EXCLUDED.es_lider, "
                   + "tiene_formacion = EXCLUDED.tiene_formacion, "
                   + "orden = EXCLUDED.orden, "
                   + "updated_at = CURRENT_TIMESTAMP";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, entry.getLineaId());
            ps.setInt(2, entry.getOperarioId());
            ps.setBoolean(3, entry.isEsLider());
            ps.setBoolean(4, entry.isTieneFormacion());
            ps.setInt(5, entry.getOrden());
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

    public void remove(int lineaId, int operarioId) throws SQLException {
        String sql = "DELETE FROM plantilla WHERE linea_id = ? AND operario_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, lineaId);
            ps.setInt(2, operarioId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void clearLinea(int lineaId) throws SQLException {
        String sql = "DELETE FROM plantilla WHERE linea_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, lineaId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void clearAll() throws SQLException {
        String sql = "DELETE FROM plantilla";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    private PlantillaEntry mapRow(ResultSet rs) throws SQLException {
        PlantillaEntry entry = new PlantillaEntry(
            rs.getInt("linea_id"),
            rs.getInt("operario_id"),
            rs.getBoolean("es_lider"),
            rs.getBoolean("tiene_formacion"),
            rs.getInt("orden")
        );
        entry.setId(rs.getInt("id"));
        entry.setOperarioCodigo(rs.getString("operario_codigo"));
        entry.setOperarioNombre(rs.getString("operario_nombre"));
        return entry;
    }
}
