package com.plantillagen.db;

import com.plantillagen.model.LineaProduccion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LineaDAO {

    public List<LineaProduccion> findAll() throws SQLException {
        List<LineaProduccion> list = new ArrayList<>();
        String sql = "SELECT id, posicion, nombre, activo, color, categoria FROM lineas_produccion WHERE activo = TRUE ORDER BY posicion";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LineaProduccion(
                    rs.getInt("id"),
                    rs.getInt("posicion"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo"),
                    rs.getString("color"),
                    rs.getString("categoria")
                ));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Optional<LineaProduccion> findById(int id) throws SQLException {
        String sql = "SELECT id, posicion, nombre, activo, color, categoria FROM lineas_produccion WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new LineaProduccion(
                    rs.getInt("id"),
                    rs.getInt("posicion"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo"),
                    rs.getString("color"),
                    rs.getString("categoria")
                ));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return Optional.empty();
    }

    public Optional<LineaProduccion> findByPosicion(int posicion) throws SQLException {
        String sql = "SELECT id, posicion, nombre, activo, color, categoria FROM lineas_produccion WHERE posicion = ? AND activo = TRUE";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, posicion);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new LineaProduccion(
                    rs.getInt("id"),
                    rs.getInt("posicion"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo"),
                    rs.getString("color"),
                    rs.getString("categoria")
                ));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return Optional.empty();
    }

    public List<LineaProduccion> findAllIncludeInactive() throws SQLException {
        List<LineaProduccion> list = new ArrayList<>();
        String sql = "SELECT id, posicion, nombre, activo, color, categoria FROM lineas_produccion ORDER BY posicion";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LineaProduccion(
                    rs.getInt("id"),
                    rs.getInt("posicion"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo"),
                    rs.getString("color"),
                    rs.getString("categoria")
                ));
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public int insert(LineaProduccion lp) throws SQLException {
        String sql = "INSERT INTO lineas_produccion (posicion, nombre, activo, color, categoria) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, lp.getPosicion());
            ps.setString(2, lp.getNombre());
            ps.setBoolean(3, lp.isActivo());
            ps.setString(4, lp.getColor());
            ps.setString(5, lp.getCategoria());
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

    public void update(LineaProduccion lp) throws SQLException {
        String sql = "UPDATE lineas_produccion SET posicion = ?, nombre = ?, activo = ?, color = ?, categoria = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, lp.getPosicion());
            ps.setString(2, lp.getNombre());
            ps.setBoolean(3, lp.isActivo());
            ps.setString(4, lp.getColor());
            ps.setString(5, lp.getCategoria());
            ps.setInt(6, lp.getId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void bulkUpdatePosiciones(List<LineaProduccion> lineas) throws SQLException {
        if (lineas.isEmpty()) return;
        StringBuilder sql = new StringBuilder(
            "UPDATE lineas_produccion SET posicion = CASE id ");
        for (LineaProduccion lp : lineas) {
            sql.append("WHEN ").append(lp.getId())
               .append(" THEN ").append(lp.getPosicion()).append(" ");
        }
        sql.append("END WHERE id IN (");
        for (int i = 0; i < lineas.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append(lineas.get(i).getId());
        }
        sql.append(")");
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("SET CONSTRAINTS ALL DEFERRED");
            ps.execute();
            ps.close();
            ps = conn.prepareStatement(sql.toString());
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void updateDetails(LineaProduccion lp) throws SQLException {
        String sql = "UPDATE lineas_produccion SET nombre = ?, activo = ?, color = ?, categoria = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, lp.getNombre());
            ps.setBoolean(2, lp.isActivo());
            ps.setString(3, lp.getColor());
            ps.setString(4, lp.getCategoria());
            ps.setInt(5, lp.getId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM lineas_produccion WHERE id = ?";
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
}
