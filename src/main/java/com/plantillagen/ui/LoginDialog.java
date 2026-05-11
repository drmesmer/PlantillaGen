package com.plantillagen.ui;

import com.plantillagen.db.UsuarioDAO;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

public class LoginDialog extends JDialog {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private boolean loginOk = false;

    public LoginDialog() {
        setTitle("PlantillaGen - Acceso");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        Image logo = loadLogo();
        if (logo != null) {
            Image scaled = logo.getScaledInstance(180, -1, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaled));
            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            mainPanel.add(logoLabel, BorderLayout.NORTH);
        }

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(labelFont);
        formPanel.add(lblUsuario, gbc);

        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        txtUsuario.setFont(fieldFont);
        formPanel.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPassword = new JLabel("Contrase\u00f1a:");
        lblPassword.setFont(labelFont);
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(fieldFont);
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnLogin = new JButton("Acceder");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(120, 36));
        btnLogin.setBackground(new Color(0, 120, 210));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        btnLogin.addActionListener(e -> doLogin());

        JButton btnCancel = new JButton("Salir");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnLogin);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String user = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Introduce usuario y contrase\u00f1a.", "Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            UsuarioDAO dao = new UsuarioDAO();
            if (dao.login(user, pass) != null) {
                loginOk = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Usuario o contrase\u00f1a incorrectos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error de conexión: " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoginOk() {
        return loginOk;
    }

    private Image loadLogo() {
        try (InputStream is = getClass().getResourceAsStream("/gfx/logo.png")) {
            if (is != null) return ImageIO.read(is);
        } catch (IOException ignored) {}
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("src/main/resources/gfx/logo.png");
            if (java.nio.file.Files.exists(path)) return ImageIO.read(path.toFile());
        } catch (IOException ignored) {}
        return null;
    }
}
