package com.plantillagen.db;

import com.plantillagen.model.Operario;
import com.plantillagen.ui.ImageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperarioDAO {

    private static boolean schemaChecked = false;

    private static void ensureSchema() {
        if (schemaChecked) return;
        schemaChecked = true;
        String[] cols = {
            "ALTER TABLE operarios ADD COLUMN IF NOT EXISTS efi INTEGER DEFAULT 50",
            "ALTER TABLE operarios ADD COLUMN IF NOT EXISTS cal INTEGER DEFAULT 50",
            "ALTER TABLE operarios ADD COLUMN IF NOT EXISTS seg INTEGER DEFAULT 50",
            "ALTER TABLE operarios ADD COLUMN IF NOT EXISTS ini INTEGER DEFAULT 50",
            "ALTER TABLE operarios ADD COLUMN IF NOT EXISTS pol INTEGER DEFAULT 50"
        };
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            for (String sql : cols) {
                stmt.execute(sql);
            }
        } catch (SQLException ignored) {}

        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COUNT(*) FROM operarios WHERE efi = 50 AND cal = 50 AND seg = 50 AND ini = 50 AND pol = 50")) {
            if (rs.next() && rs.getInt(1) > 0) {
                stmt.execute("UPDATE operarios SET "
                    + "efi = FLOOR(RANDOM() * 99 + 1)::INT, "
                    + "cal = FLOOR(RANDOM() * 99 + 1)::INT, "
                    + "seg = FLOOR(RANDOM() * 99 + 1)::INT, "
                    + "ini = FLOOR(RANDOM() * 99 + 1)::INT, "
                    + "pol = FLOOR(RANDOM() * 99 + 1)::INT");
            }
        } catch (SQLException ignored) {}
    }

    public List<Operario> findAll() throws SQLException {
        ensureSchema();
        List<Operario> list = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, activo, efi, cal, seg, ini, pol "
                   + "FROM operarios WHERE activo = TRUE ORDER BY codigo";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Operario op = mapOperario(rs);
                op.setFoto(ImageUtil.createPlaceholder(op.getCodigo()));
                list.add(op);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Optional<Operario> findById(int id) throws SQLException {
        String sql = "SELECT id, codigo, nombre, activo, efi, cal, seg, ini, pol "
                   + "FROM operarios WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                Operario op = mapOperario(rs);
                op.setFoto(ImageUtil.createPlaceholder(op.getCodigo()));
                return Optional.of(op);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return Optional.empty();
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM operarios WHERE activo = TRUE";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return 0;
    }

    public int insert(Operario op) throws SQLException {
        String sql = "INSERT INTO operarios (codigo, nombre, efi, cal, seg, ini, pol) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, op.getCodigo());
            ps.setString(2, op.getNombre());
            ps.setInt(3, op.getEfi());
            ps.setInt(4, op.getCal());
            ps.setInt(5, op.getSeg());
            ps.setInt(6, op.getIni());
            ps.setInt(7, op.getPol());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                op.setId(rs.getInt(1));
                return op.getId();
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return -1;
    }

    public void update(Operario op) throws SQLException {
        String sql = "UPDATE operarios SET codigo = ?, nombre = ?, activo = ?, "
                   + "efi = ?, cal = ?, seg = ?, ini = ?, pol = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, op.getCodigo());
            ps.setString(2, op.getNombre());
            ps.setBoolean(3, op.isActivo());
            ps.setInt(4, op.getEfi());
            ps.setInt(5, op.getCal());
            ps.setInt(6, op.getSeg());
            ps.setInt(7, op.getIni());
            ps.setInt(8, op.getPol());
            ps.setInt(9, op.getId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.close(null, ps, conn);
        }
    }

    public List<Operario> findAllIncludeInactive() throws SQLException {
        List<Operario> list = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, activo, efi, cal, seg, ini, pol "
                   + "FROM operarios ORDER BY codigo";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Operario op = mapOperario(rs);
                op.setFoto(ImageUtil.createPlaceholder(op.getCodigo()));
                list.add(op);
            }
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM operarios WHERE id = ?";
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

    private Operario mapOperario(ResultSet rs) throws SQLException {
        Operario op = new Operario(
            rs.getInt("id"),
            rs.getString("codigo"),
            rs.getString("nombre")
        );
        op.setActivo(rs.getBoolean("activo"));
        op.setEfi(rs.getInt("efi"));
        op.setCal(rs.getInt("cal"));
        op.setSeg(rs.getInt("seg"));
        op.setIni(rs.getInt("ini"));
        op.setPol(rs.getInt("pol"));
        return op;
    }
}
