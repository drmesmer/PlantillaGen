package com.plantillagen.db;

import com.plantillagen.model.PlantillaEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlantillaDetalleDAO {

    public List<PlantillaEntry> findByPlantillaIdAndTurno(int plantillaId, int turnoId)
            throws SQLException {
        List<PlantillaEntry> list = new ArrayList<>();
        String sql = "SELECT pd.id, pd.plantilla_id, pd.linea_id, pd.operario_id, "
                   + "pd.es_lider, pd.tiene_formacion, pd.orden, pd.turno_id, "
                   + "o.codigo AS operario_codigo, o.nombre AS operario_nombre "
                   + "FROM plantilla_detalle pd "
                   + "JOIN operarios o ON o.id = pd.operario_id "
                   + "WHERE pd.plantilla_id = ? AND pd.turno_id = ? "
                   + "ORDER BY pd.linea_id, pd.orden";
        return queryEntries(sql, plantillaId, turnoId);
    }

    public List<PlantillaEntry> findByPlantillaId(int plantillaId) throws SQLException {
        List<PlantillaEntry> list = new ArrayList<>();
        String sql = "SELECT pd.id, pd.plantilla_id, pd.linea_id, pd.operario_id, "
                   + "pd.es_lider, pd.tiene_formacion, pd.orden, pd.turno_id, "
                   + "o.codigo AS operario_codigo, o.nombre AS operario_nombre "
                   + "FROM plantilla_detalle pd "
                   + "JOIN operarios o ON o.id = pd.operario_id "
                   + "WHERE pd.plantilla_id = ? "
                   + "ORDER BY pd.turno_id, pd.linea_id, pd.orden";
        return queryEntries(sql, plantillaId, 0);
    }

    private List<PlantillaEntry> queryEntries(String sql, int plantillaId, int turnoId)
            throws SQLException {
        List<PlantillaEntry> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, plantillaId);
            if (turnoId > 0) ps.setInt(2, turnoId);
            rs = ps.executeQuery();
            while (rs.next()) {
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
                entry.setTurnoId(rs.getInt("turno_id"));
                list.add(entry);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public void save(int plantillaId, PlantillaEntry entry) throws SQLException {
        String sql = "INSERT INTO plantilla_detalle (plantilla_id, linea_id, operario_id, "
                   + "es_lider, tiene_formacion, orden, turno_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                   + "ON CONFLICT (plantilla_id, linea_id, operario_id, turno_id) DO UPDATE SET "
                   + "es_lider = EXCLUDED.es_lider, "
                   + "tiene_formacion = EXCLUDED.tiene_formacion, "
                   + "orden = EXCLUDED.orden";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, plantillaId);
            ps.setInt(2, entry.getLineaId());
            ps.setInt(3, entry.getOperarioId());
            ps.setBoolean(4, entry.isEsLider());
            ps.setBoolean(5, entry.isTieneFormacion());
            ps.setInt(6, entry.getOrden());
            ps.setInt(7, entry.getTurnoId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void deleteByPlantillaIdAndTurno(int plantillaId, int turnoId)
            throws SQLException {
        String sql = "DELETE FROM plantilla_detalle WHERE plantilla_id = ? AND turno_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, plantillaId);
            ps.setInt(2, turnoId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void deleteByPlantillaId(int plantillaId) throws SQLException {
        String sql = "DELETE FROM plantilla_detalle WHERE plantilla_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, plantillaId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }
}
