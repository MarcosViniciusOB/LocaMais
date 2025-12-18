import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

/**
 * Classe Principal da aplicação Loca Mais.
 * <p>
 * Responsável por:
 * <ul>
 * <li>Inicializar a janela principal (JFrame).</li>
 * <li>Gerenciar a navegação entre telas usando CardLayout.</li>
 * <li>Fornecer a conexão centralizada com o Banco de Dados.</li>
 * <li>Definir estilos globais visuais (Cores e Botões).</li>
 * </ul>
 * @author Marcos Vinicius
 */
public class Main extends JFrame {

    // Gerenciador de layout para alternar entre painéis (telas)
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Referências para os painéis da aplicação
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private ClienteFrame clientePanel;
    private ReservaFrame reservaPanel;
    private ReservaViewFrame reservaViewPanel;

    /**
     * Construtor da Janela Principal.
     * Configura as propriedades do JFrame e inicializa todas as telas.
     */
    public Main() {
        setTitle(LanguageManager.get("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Instancia os painéis passando 'this' para permitir navegação
        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        clientePanel = new ClienteFrame(this);
        reservaPanel = new ReservaFrame(this);
        reservaViewPanel = new ReservaViewFrame(this);

        mainContainer.add(loginPanel, "LOGIN");
        mainContainer.add(dashboardPanel, "DASHBOARD");
        mainContainer.add(clientePanel, "CLIENTES");
        mainContainer.add(reservaPanel, "NOVA_RESERVA");
        mainContainer.add(reservaViewPanel, "VER_RESERVAS");

        add(mainContainer);
        cardLayout.show(mainContainer, "LOGIN");
        setVisible(true);
    }

    public void mostrarDashboard() {
        dashboardPanel.carregarDados(null);
        cardLayout.show(mainContainer, "DASHBOARD");
    }
    public void mostrarClientes() {
        clientePanel.carregarClientes(null);
        cardLayout.show(mainContainer, "CLIENTES");
    }
    public void mostrarNovaReserva() {
        reservaPanel.atualizarDadosIniciais();
        cardLayout.show(mainContainer, "NOVA_RESERVA");
    }
    public void mostrarVerReservas() {
        reservaViewPanel.carregarReservas(null);
        cardLayout.show(mainContainer, "VER_RESERVAS");
    }
    public void fazerLogout() {
        cardLayout.show(mainContainer, "LOGIN");
    }

    /**
     * Reinicia a aplicação (útil para troca de idioma).
     * */
    public void reiniciarAplicacao() {
        dispose();
        new Main();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }


    /**
     * Classe interna para gerenciamento da conexão JDBC.
     * Utiliza o driver MySQL.
     */
    public static class Conexao {
        private static final String URL = "jdbc:mysql://localhost:3306/locamais";
        private static final String USER = "root";
        private static final String PASSWORD = "";

        /**
         * Estabelece uma conexão com o banco de dados.
         * @return Connection objeto de conexão ou null se falhar.
         */
        public static Connection conectar() {
            try { return DriverManager.getConnection(URL, USER, PASSWORD); }
            catch (SQLException e) { System.err.println("Erro Banco: " + e.getMessage()); return null; }
        }
    }

    /**
     * Classe utilitária para padronização visual (Design System).
     */
    public static final class GlobalStyles {
        public static final Color COLOR_GREEN = new Color(76, 175, 80);
        public static final Color COLOR_RED = new Color(244, 67, 54);
        public static final Color COLOR_BLUE = new Color(33, 150, 243);
        public static final Color COLOR_YELLOW = new Color(255, 193, 7);
        public static final Color COLOR_BLACK = Color.BLACK;
        public static final Color COLOR_WHITE = Color.WHITE;

        public static void styleButton(JButton button, Color background) {
            styleButton(button, background, COLOR_WHITE);
        }
        public static void styleButton(JButton button, Color background, Color foreground) {
            button.setBackground(background);
            button.setForeground(foreground);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public static void styleButtonIcon(JButton button, Color background) {
            button.setBackground(background);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setMargin(new Insets(2, 8, 2, 8));
        }
    }
}

class LoginPanel extends JPanel {
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private Main main;
    private JComboBox<String> cmbLang;

    public LoginPanel(Main main) {
        this.main = main;
        setLayout(new GridBagLayout());
        setBackground(new Color(230, 230, 230));

        JPanel card = new JPanel(new GridLayout(0, 1, 10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        ));
        card.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel(LanguageManager.get("login.title"), SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));

        txtEmail = new JTextField(15);
        txtEmail.setBorder(BorderFactory.createTitledBorder(LanguageManager.get("login.email")));
        txtSenha = new JPasswordField(15);
        txtSenha.setBorder(BorderFactory.createTitledBorder(LanguageManager.get("login.pass")));

        JButton btnEntrar = new JButton(LanguageManager.get("login.enter"));
        Main.GlobalStyles.styleButton(btnEntrar, Main.GlobalStyles.COLOR_GREEN);

        btnEntrar.addActionListener(e -> logar());

        String[] langs = {"Português", "English", "Español"};
        cmbLang = new JComboBox<>(langs);

        // Lógica para selecionar o item correto ao abrir a tela
        String langAtual = LanguageManager.getLocale().getLanguage();
        if(langAtual.equals("en")) cmbLang.setSelectedIndex(1);
        else if(langAtual.equals("es")) cmbLang.setSelectedIndex(2);
        else cmbLang.setSelectedIndex(0);

        // Lógica de troca de idioma
        cmbLang.addActionListener(e -> {
            int i = cmbLang.getSelectedIndex();
            if(i == 0) LanguageManager.setLanguage("pt", "BR");
            else if(i == 1) LanguageManager.setLanguage("en", "US");
            else if(i == 2) LanguageManager.setLanguage("es", "ES"); // Define Espanhol

            main.reiniciarAplicacao();
        });

        JPanel langPanel = new JPanel(new BorderLayout());
        langPanel.setBackground(Color.WHITE);
        langPanel.add(new JLabel("Idioma / Language: "), BorderLayout.WEST);
        langPanel.add(cmbLang, BorderLayout.CENTER);

        card.add(lblTitulo);
        card.add(txtEmail);
        card.add(txtSenha);
        card.add(langPanel);
        card.add(btnEntrar);

        add(card);
    }

    private void logar() {
        String email = txtEmail.getText();
        String senha = new String(txtSenha.getPassword());
        Connection conn = Main.Conexao.conectar();
        if(conn == null) return;
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM tab_usuarios WHERE email=? AND senha=?");
            pst.setString(1, email); pst.setString(2, senha);
            if (pst.executeQuery().next()) main.mostrarDashboard();
            else JOptionPane.showMessageDialog(this, LanguageManager.get("msg.login.fail"), LanguageManager.get("title.error"), JOptionPane.ERROR_MESSAGE);
            conn.close();
        } catch(Exception e) { e.printStackTrace(); }
    }
}