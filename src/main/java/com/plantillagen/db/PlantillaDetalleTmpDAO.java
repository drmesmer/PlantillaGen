package com.plantillagen.db;

import com.plantillagen.model.PlantillaEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlantillaDetalleTmpDAO {

    public List<PlantillaEntry> findByPlantillaIdAndTurno(int plantillaId, int turnoId)
            throws SQLException {
        List<PlantillaEntry> list = new ArrayList<>();
        String sql = "SELECT id, plantilla_id, linea_id, operario_id, "
                   + "es_lider, tiene_formacion, orden, turno_id "
                   + "FROM plantilla_detalle_tmp "
                   + "WHERE plantilla_id = ? AND turno_id = ? "
                   + "ORDER BY linea_id, orden";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, plantillaId);
            ps.setInt(2, turnoId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public void save(int plantillaId, PlantillaEntry entry) throws SQLException {
        String sql = "INSERT INTO plantilla_detalle_tmp "
                   + "(plantilla_id, linea_id, operario_id, es_lider, tiene_formacion, orden, turno_id) "
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
        String sql = "DELETE FROM plantilla_detalle_tmp WHERE plantilla_id = ? AND turno_id = ?";
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

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM plantilla_detalle_tmp";
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

    public void deleteByPlantillaId(int plantillaId) throws SQLException {
        String sql = "DELETE FROM plantilla_detalle_tmp WHERE plantilla_id = ?";
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

    public void copyToPlantillaDetalle(int plantillaId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(
                "DELETE FROM plantilla_detalle WHERE plantilla_id = ?");
            ps.setInt(1, plantillaId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                "INSERT INTO plantilla_detalle (plantilla_id, linea_id, operario_id, "
                + "es_lider, tiene_formacion, orden, turno_id) "
                + "SELECT plantilla_id, linea_id, operario_id, "
                + "es_lider, tiene_formacion, orden, turno_id "
                + "FROM plantilla_detalle_tmp WHERE plantilla_id = ?");
            ps.setInt(1, plantillaId);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void copyFromPlantillaDetalle(int plantillaId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(
                "DELETE FROM plantilla_detalle_tmp WHERE plantilla_id = ?");
            ps.setInt(1, plantillaId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                "INSERT INTO plantilla_detalle_tmp (plantilla_id, linea_id, operario_id, "
                + "es_lider, tiene_formacion, orden, turno_id) "
                + "SELECT plantilla_id, linea_id, operario_id, "
                + "es_lider, tiene_formacion, orden, turno_id "
                + "FROM plantilla_detalle WHERE plantilla_id = ?");
            ps.setInt(1, plantillaId);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
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
        entry.setTurnoId(rs.getInt("turno_id"));
        return entry;
    }
}
