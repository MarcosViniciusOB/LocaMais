import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

/**
 * Tela de gerenciamento de Clientes.
 * Responsável pela listagem, cadastro, edição e exclusão de clientes,
 * com suporte a filtros de busca e validação de CPF.
 * @author Marcos Vinicius
 */
public class ClienteFrame extends JPanel {
    private JTable tabelaClientes;
    private DefaultTableModel modeloClientes;
    private Main parentFrame;
    private JTextField txtBuscaCliente;
    private JComboBox<String> cmbFiltro;

    public ClienteFrame(Main parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245,245,245));
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton btnVoltar = new JButton(LanguageManager.get("menu.back"));
        Main.GlobalStyles.styleButton(btnVoltar, Color.GRAY);
        btnVoltar.addActionListener(e -> parentFrame.mostrarDashboard());

        JLabel lblTitulo = new JLabel(LanguageManager.get("client.title"), SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));

        header.add(btnVoltar, BorderLayout.WEST);
        header.add(lblTitulo, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNovo = new JButton(LanguageManager.get("client.new"));
        Main.GlobalStyles.styleButton(btnNovo, Main.GlobalStyles.COLOR_GREEN);
        btnNovo.addActionListener(e -> cadastrarCliente());

        toolbar.add(btnNovo);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(new JLabel(" | " + LanguageManager.get("client.search") + ": "));

        String[] opcoes = {
                LanguageManager.get("filter.all"),
                LanguageManager.get("filter.client.name"),
                LanguageManager.get("filter.client.cpf")
        };
        cmbFiltro = new JComboBox<>(opcoes);
        cmbFiltro.setBackground(Color.WHITE);
        toolbar.add(cmbFiltro);

        txtBuscaCliente = new JTextField(20);
        txtBuscaCliente.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int tipo = cmbFiltro.getSelectedIndex();
                char c = e.getKeyChar();
                if (tipo == 2) {
                    String permitidos = "0123456789.-";
                    if (!permitidos.contains(c + "") && c != KeyEvent.VK_BACK_SPACE) e.consume();
                } else if (tipo == 1) {
                    if (Character.isDigit(c)) e.consume();
                }
            }
        });
        cmbFiltro.addActionListener(e -> txtBuscaCliente.setText(""));

        JButton btnBusca = new JButton(LanguageManager.get("client.search"));
        Main.GlobalStyles.styleButton(btnBusca, Main.GlobalStyles.COLOR_BLUE);
        btnBusca.addActionListener(e -> carregarClientes(txtBuscaCliente.getText()));
        txtBuscaCliente.addActionListener(e -> carregarClientes(txtBuscaCliente.getText()));

        toolbar.add(txtBuscaCliente); toolbar.add(btnBusca);

        JPanel panelTabela = new JPanel(new BorderLayout());
        panelTabela.add(toolbar, BorderLayout.NORTH);

        String[] cols = {
                LanguageManager.get("col.id"),
                LanguageManager.get("col.name"),
                LanguageManager.get("col.cpf"),
                LanguageManager.get("col.birth"),
                LanguageManager.get("col.actions")
        };

        modeloClientes = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) {
                String nomeColuna = getColumnName(col);
                return nomeColuna.equals(LanguageManager.get("col.actions"));
            }
        };
        tabelaClientes = new JTable(modeloClientes);
        tabelaClientes.setRowHeight(35);
        tabelaClientes.putClientProperty("JTable.autoStartsEdit", Boolean.TRUE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<4; i++) tabelaClientes.getColumnModel().getColumn(i).setCellRenderer(center);

        tabelaClientes.getColumn(LanguageManager.get("col.actions")).setCellRenderer(new ButtonsRenderer());
        tabelaClientes.getColumn(LanguageManager.get("col.actions")).setCellEditor(new ButtonsEditor(new JCheckBox()));

        panelTabela.add(new JScrollPane(tabelaClientes), BorderLayout.CENTER);
        panelTabela.setBorder(BorderFactory.createEmptyBorder(10,20,20,20));
        add(panelTabela, BorderLayout.CENTER);
    }

    public void carregarClientes(String filtroTexto) {
        modeloClientes.setRowCount(0);
        Connection conn = Main.Conexao.conectar();
        if(conn == null) return;
        try {
            String sql = "SELECT * FROM tab_clientes";
            int tipoFiltro = cmbFiltro.getSelectedIndex();
            boolean temFiltro = filtroTexto != null && !filtroTexto.trim().isEmpty();

            if (temFiltro) {
                switch (tipoFiltro) {
                    case 0: sql += " WHERE nome LIKE ? OR cpf LIKE ?"; break;
                    case 1: sql += " WHERE nome LIKE ?"; break;
                    case 2: sql += " WHERE cpf LIKE ?"; break;
                }
            }
            switch (tipoFiltro) {
                case 1: sql += " ORDER BY nome ASC"; break;
                case 2: sql += " ORDER BY cpf ASC"; break;
                default: sql += " ORDER BY id ASC"; break;
            }

            PreparedStatement pst = conn.prepareStatement(sql);
            if (temFiltro) {
                String buscaLike = "%" + filtroTexto + "%";
                if (tipoFiltro == 0) { pst.setString(1, buscaLike); pst.setString(2, buscaLike); }
                else { pst.setString(1, buscaLike); }
            }

            ResultSet rs = pst.executeQuery();

            while(rs.next()) {
                // 1. Instancia o objeto e preenche com dados do banco
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setCpf(rs.getString("cpf"));

                // Conversão de data do SQL para String usando o utilitário
                java.sql.Date dataSQL = rs.getDate("data_nasc");
                c.setData_nasc(dataSQL != null ? DateUtil.formatarData(dataSQL) : "");

                // 2. Usa os Getters do objeto para preencher a linha da tabela
                modeloClientes.addRow(new Object[]{
                        c.getId(),
                        c.getNome(),
                        ValidadorCPF.formatarCPF(c.getCpf()), // Formata visualmente
                        c.getData_nasc(),
                        null // Coluna de ações
                });
            }
            conn.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void validarCampos(JTextField nome, JTextField cpf, JTextField data) {
        nome.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) { if (Character.isDigit(e.getKeyChar())) e.consume(); }
        });
        cpf.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String permitidos = "0123456789.-";
                if (!permitidos.contains(e.getKeyChar() + "")) e.consume();
            }
        });
        data.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String permitidos = "0123456789/";
                if (!permitidos.contains(e.getKeyChar() + "")) e.consume();
            }
        });
    }

    private void cadastrarCliente() {
        JTextField nome = new JTextField();
        JTextField cpf = new JTextField();
        JTextField data = new JTextField();

        validarCampos(nome, cpf, data);

        String formatoData = LanguageManager.getDateFormat();

        JPanel p = new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel(LanguageManager.get("col.name"))); p.add(nome);
        p.add(new JLabel(LanguageManager.get("col.cpf"))); p.add(cpf);
        p.add(new JLabel(LanguageManager.get("col.birth") + " ("+formatoData+"):")); p.add(data);

        while(true) {
            int res = JOptionPane.showConfirmDialog(this, p, LanguageManager.get("client.dialog.new"), JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            if (!ValidadorCPF.isCPF(cpf.getText())) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error.cpf"), LanguageManager.get("title.error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                Connection conn = Main.Conexao.conectar();
                PreparedStatement pst = conn.prepareStatement("INSERT INTO tab_clientes (nome, cpf, data_nasc) VALUES (?,?,?)");
                pst.setString(1, nome.getText());
                pst.setString(2, cpf.getText());
                pst.setDate(3, DateUtil.converterEValidarData(data.getText()));
                pst.executeUpdate();
                conn.close();
                carregarClientes(null);
                JOptionPane.showMessageDialog(this, LanguageManager.get("msg.success"));
                break;
            } catch(Exception e) { tratarErroBD(e); }
        }
    }

    private void abrirDialogoEdicao(int row) {
        int idAtual = (int) tabelaClientes.getValueAt(row, 0);
        String nomeAtual = (String) tabelaClientes.getValueAt(row, 1);
        String cpfAtual = (String) tabelaClientes.getValueAt(row, 2);
        String dataAtual = (String) tabelaClientes.getValueAt(row, 3);

        String formatoData = LanguageManager.getDateFormat();

        JTextField nome = new JTextField(nomeAtual);
        JTextField data = new JTextField(dataAtual);

        nome.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) { if (Character.isDigit(e.getKeyChar())) e.consume(); }
        });
        data.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String permitidos = "0123456789/";
                if (!permitidos.contains(e.getKeyChar() + "")) e.consume();
            }
        });

        JPanel p = new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel(LanguageManager.get("col.name"))); p.add(nome);
        p.add(new JLabel(LanguageManager.get("col.cpf")+" (" + LanguageManager.get("label.fixed") + "):"));
        p.add(new JLabel(cpfAtual));
        p.add(new JLabel(LanguageManager.get("col.birth")+" ("+formatoData+"):")); p.add(data);

        if(JOptionPane.showConfirmDialog(this, p, LanguageManager.get("client.dialog.edit"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Connection conn = Main.Conexao.conectar();
                PreparedStatement pst = conn.prepareStatement("UPDATE tab_clientes SET nome=?, data_nasc=? WHERE id=?");
                pst.setString(1, nome.getText());
                pst.setDate(2, DateUtil.converterEValidarData(data.getText()));
                pst.setInt(3, idAtual);
                pst.executeUpdate();
                conn.close();
                carregarClientes(null);
                JOptionPane.showMessageDialog(this, LanguageManager.get("msg.success"));
            } catch(Exception e) { tratarErroBD(e); }
        }
    }

    private void tratarErroBD(Exception ex) {
        String msg = ex.getMessage();
        if (msg.contains("foreign key") || msg.contains("constraint")) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error.constraint"), LanguageManager.get("title.error"), JOptionPane.ERROR_MESSAGE);
        } else if (msg.contains("Duplicate entry")) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error.duplicate"), LanguageManager.get("title.error"), JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error") + " " + msg);
        }
    }

    class ButtonsRenderer extends JPanel implements TableCellRenderer {
        JButton btnEdit = new JButton("✏️");
        JButton btnDel = new JButton("🗑️");
        public ButtonsRenderer() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            Main.GlobalStyles.styleButtonIcon(btnEdit, Main.GlobalStyles.COLOR_BLUE);
            Main.GlobalStyles.styleButtonIcon(btnDel, Main.GlobalStyles.COLOR_RED);
            add(btnEdit); add(btnDel);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? t.getSelectionBackground() : t.getBackground());
            return this;
        }
    }
    class ButtonsEditor extends DefaultCellEditor {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JButton btnEdit = new JButton("✏️");
        JButton btnDel = new JButton("🗑️");
        public ButtonsEditor(JCheckBox chk) {
            super(chk);
            Main.GlobalStyles.styleButtonIcon(btnEdit, Main.GlobalStyles.COLOR_BLUE);
            Main.GlobalStyles.styleButtonIcon(btnDel, Main.GlobalStyles.COLOR_RED);
            btnEdit.addActionListener(e -> {
                int r = tabelaClientes.getSelectedRow();
                if(r >= 0) abrirDialogoEdicao(r);
                fireEditingStopped();
            });
            btnDel.addActionListener(e -> {
                int r = tabelaClientes.getSelectedRow();
                if(r >= 0) {
                    int id = (int) tabelaClientes.getValueAt(r, 0);
                    String nome = (String) tabelaClientes.getValueAt(r, 1);
                    if(JOptionPane.showConfirmDialog(null, LanguageManager.get("btn.delete") + " '" + nome + "'?", LanguageManager.get("title.attention"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            Connection conn = Main.Conexao.conectar();
                            conn.createStatement().execute("DELETE FROM tab_clientes WHERE id="+id);
                            conn.close();
                            carregarClientes(null);
                            JOptionPane.showMessageDialog(null, LanguageManager.get("msg.deleted"));
                        } catch(Exception ex) { tratarErroBD(ex); }
                    }
                }
                fireEditingStopped();
            });
            p.add(btnEdit); p.add(btnDel);
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            p.setBackground(t.getSelectionBackground());
            return p;
        }
    }
}