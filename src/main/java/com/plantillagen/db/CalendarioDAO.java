package com.plantillagen.db;

import com.plantillagen.model.CalendarioEntry;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarioDAO {

    private static boolean schemaChecked = false;

    private static void ensureSchema() {
        if (schemaChecked) return;
        schemaChecked = true;
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE calendario "
                + "ADD COLUMN IF NOT EXISTS plantilla_id INTEGER");
            stmt.execute("ALTER TABLE calendario "
                + "DROP CONSTRAINT IF EXISTS calendario_linea_id_turno_id_fecha_key");
            stmt.execute("ALTER TABLE calendario "
                + "DROP CONSTRAINT IF EXISTS calendario_plantilla_id_fecha_key");
            stmt.execute("ALTER TABLE calendario "
                + "ADD CONSTRAINT calendario_plantilla_id_fecha_key "
                + "UNIQUE (plantilla_id, fecha)");
        } catch (SQLException ignored) {}
    }

    public List<CalendarioEntry> findByYear(int year) throws SQLException {
        ensureSchema();
        List<CalendarioEntry> list = new ArrayList<>();
        String sql = "SELECT c.id, c.plantilla_id, c.fecha, c.activo, "
                   + "p.nombre AS plantilla_nombre "
                   + "FROM calendario c "
                   + "LEFT JOIN plantillas p ON p.id = c.plantilla_id "
                   + "WHERE EXTRACT(YEAR FROM c.fecha) = ? "
                   + "ORDER BY c.fecha";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, year);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public void save(CalendarioEntry entry) throws SQLException {
        ensureSchema();
        String sql = "INSERT INTO calendario (plantilla_id, fecha, activo) "
                   + "VALUES (?, ?, ?) "
                   + "ON CONFLICT (plantilla_id, fecha) DO UPDATE SET "
                   + "activo = EXCLUDED.activo";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, entry.getPlantillaId());
            ps.setDate(2, Date.valueOf(entry.getFecha()));
            ps.setBoolean(3, entry.isActivo());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void deleteByPlantillaAndDate(int plantillaId, LocalDate fecha)
            throws SQLException {
        ensureSchema();
        String sql = "DELETE FROM calendario WHERE plantilla_id = ? AND fecha = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, plantillaId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    private CalendarioEntry mapRow(ResultSet rs) throws SQLException {
        CalendarioEntry entry = new CalendarioEntry();
        entry.setId(rs.getInt("id"));
        entry.setPlantillaId(rs.getInt("plantilla_id"));
        entry.setFecha(rs.getDate("fecha").toLocalDate());
        entry.setActivo(rs.getBoolean("activo"));
        try { entry.setPlantillaNombre(rs.getString("plantilla_nombre")); }
        catch (SQLException ignored) {}
        return entry;
    }
}
