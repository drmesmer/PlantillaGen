package com.plantillagen.db;

import com.plantillagen.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UsuarioDAO {

    private static boolean schemaChecked = false;

    private static void ensureSchema() {
        if (schemaChecked) return;
        schemaChecked = true;
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios ("
                + "id SERIAL PRIMARY KEY, "
                + "codigo VARCHAR(50) NOT NULL UNIQUE, "
                + "password VARCHAR(100) NOT NULL, "
                + "ultima_sesion TIMESTAMP)");
        } catch (SQLException e) {
            System.err.println("Error creando tabla usuarios: " + e.getMessage());
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO usuarios (codigo, password) "
                + "SELECT 'admin', 'admin' "
                + "WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE codigo = 'admin')");
        } catch (SQLException ignored) {}
    }

    public Usuario login(String codigo, String password) throws SQLException {
        ensureSchema();
        String sql = "SELECT id, codigo, password, ultima_sesion FROM usuarios "
                   + "WHERE codigo = ? AND password = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, codigo);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = mapRow(rs);
                updateUltimaSesion(u.getId());
                return u;
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return null;
    }

    private void updateUltimaSesion(int id) throws SQLException {
        String sql = "UPDATE usuarios SET ultima_sesion = CURRENT_TIMESTAMP WHERE id = ?";
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

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario(
            rs.getString("codigo"),
            rs.getString("password")
        );
        u.setId(rs.getInt("id"));
        u.setUltimaSesion(rs.getTimestamp("ultima_sesion"));
        return u;
    }
}
