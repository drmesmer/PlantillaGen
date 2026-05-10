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

    public List<CalendarioEntry> findByMonth(int year, int month) throws SQLException {
        List<CalendarioEntry> list = new ArrayList<>();
        String sql = "SELECT c.id, c.linea_id, c.turno_id, c.fecha, c.activo, "
                   + "l.nombre AS linea_nombre, t.nombre AS turno_nombre "
                   + "FROM calendario c "
                   + "LEFT JOIN lineas_produccion l ON l.id = c.linea_id "
                   + "LEFT JOIN turnos t ON t.id = c.turno_id "
                   + "WHERE EXTRACT(YEAR FROM c.fecha) = ? AND EXTRACT(MONTH FROM c.fecha) = ? "
                   + "ORDER BY c.fecha, c.turno_id, c.linea_id";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, year);
            ps.setInt(2, month);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<CalendarioEntry> findByYear(int year) throws SQLException {
        List<CalendarioEntry> list = new ArrayList<>();
        String sql = "SELECT c.id, c.linea_id, c.turno_id, c.fecha, c.activo, "
                   + "l.nombre AS linea_nombre, t.nombre AS turno_nombre "
                   + "FROM calendario c "
                   + "LEFT JOIN lineas_produccion l ON l.id = c.linea_id "
                   + "LEFT JOIN turnos t ON t.id = c.turno_id "
                   + "WHERE EXTRACT(YEAR FROM c.fecha) = ? "
                   + "ORDER BY c.fecha, c.turno_id, c.linea_id";
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
        String sql = "INSERT INTO calendario (linea_id, turno_id, fecha, activo) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON CONFLICT (linea_id, turno_id, fecha) DO UPDATE SET "
                   + "activo = EXCLUDED.activo";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, entry.getLineaId());
            ps.setInt(2, entry.getTurnoId());
            ps.setDate(3, Date.valueOf(entry.getFecha()));
            ps.setBoolean(4, entry.isActivo());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM calendario WHERE id = ?";
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

    private CalendarioEntry mapRow(ResultSet rs) throws SQLException {
        CalendarioEntry entry = new CalendarioEntry();
        entry.setId(rs.getInt("id"));
        entry.setLineaId(rs.getInt("linea_id"));
        entry.setTurnoId(rs.getInt("turno_id"));
        entry.setFecha(rs.getDate("fecha").toLocalDate());
        entry.setActivo(rs.getBoolean("activo"));
        try { entry.setLineaNombre(rs.getString("linea_nombre")); } catch (SQLException ignored) {}
        try { entry.setTurnoNombre(rs.getString("turno_nombre")); } catch (SQLException ignored) {}
        return entry;
    }
}
