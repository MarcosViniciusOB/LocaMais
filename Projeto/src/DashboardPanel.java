import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Painel principal (Dashboard) para gerenciamento da frota de veículos.
 * <p>
 * Funcionalidades:
 * <ul>
 * <li>Tabela CRUD de Veículos.</li>
 * <li>Filtros dinâmicos (Marca, Modelo, ID).</li>
 * <li>Botões de Ação dentro da tabela (Editar/Excluir).</li>
 * </ul>
 */
class DashboardPanel extends JPanel {
    private Main main;
    private DefaultTableModel modelo;
    private JTable tabela;
    private JTextField txtBusca;
    private JComboBox<String> cmbFiltro;

    public DashboardPanel(Main main) {
        this.main = main;
        setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        menu.setBackground(Color.WHITE);
        menu.setBorder(BorderFactory.createMatteBorder(0,0,2,0, new Color(200,200,200)));

        JButton btnCad = new JButton(LanguageManager.get("dash.btn.new"));
        JButton btnCli = new JButton(LanguageManager.get("menu.client"));
        JButton btnReserva = new JButton(LanguageManager.get("menu.reserve"));
        JButton btnVer = new JButton(LanguageManager.get("menu.view"));
        JButton btnSair = new JButton(LanguageManager.get("menu.exit"));

        Main.GlobalStyles.styleButton(btnCad, Main.GlobalStyles.COLOR_BLUE);
        Main.GlobalStyles.styleButton(btnCli, Main.GlobalStyles.COLOR_YELLOW, Color.BLACK);
        Main.GlobalStyles.styleButton(btnReserva, Main.GlobalStyles.COLOR_GREEN);
        Main.GlobalStyles.styleButton(btnVer, Main.GlobalStyles.COLOR_GREEN);
        Main.GlobalStyles.styleButton(btnSair, Main.GlobalStyles.COLOR_RED);

        btnCad.addActionListener(e -> abrirDialogoCadastro());
        btnCli.addActionListener(e -> main.mostrarClientes());
        btnReserva.addActionListener(e -> main.mostrarNovaReserva());
        btnVer.addActionListener(e -> main.mostrarVerReservas());
        btnSair.addActionListener(e -> main.fazerLogout());

        menu.add(btnCad); menu.add(btnCli); menu.add(btnReserva); menu.add(btnVer);
        menu.add(Box.createHorizontalStrut(50)); menu.add(btnSair);
        add(menu, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(10, 10));
        corpo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBusca.add(new JLabel(LanguageManager.get("dash.filter")));

        String[] opcoes = {
                LanguageManager.get("filter.all"),
                LanguageManager.get("filter.brand"),
                LanguageManager.get("filter.model"),
                LanguageManager.get("filter.id")
        };
        cmbFiltro = new JComboBox<>(opcoes);
        cmbFiltro.setBackground(Color.WHITE);

        txtBusca = new JTextField(30);
        txtBusca.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                int tipo = cmbFiltro.getSelectedIndex();
                char c = e.getKeyChar();
                if (tipo == 3) { if (!Character.isDigit(c)) e.consume(); }
                else if (tipo == 1 || tipo == 2) { if (Character.isDigit(c)) e.consume(); }
            }
        });
        cmbFiltro.addActionListener(e -> txtBusca.setText(""));

        JButton btnBusca = new JButton(LanguageManager.get("dash.btn.search"));
        Main.GlobalStyles.styleButton(btnBusca, Main.GlobalStyles.COLOR_BLUE);
        btnBusca.addActionListener(e -> carregarDados(txtBusca.getText()));
        txtBusca.addActionListener(e -> carregarDados(txtBusca.getText()));

        painelBusca.add(cmbFiltro); painelBusca.add(txtBusca); painelBusca.add(btnBusca);
        corpo.add(painelBusca, BorderLayout.NORTH);

        String[] cols = {
                LanguageManager.get("col.id"),
                LanguageManager.get("col.brand"),
                LanguageManager.get("col.model"),
                LanguageManager.get("col.color"),
                LanguageManager.get("col.year"),
                LanguageManager.get("col.qty"),
                LanguageManager.get("col.actions")
        };

        modelo = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) {
                String nomeColuna = getColumnName(col);
                return nomeColuna.equals(LanguageManager.get("col.actions"));
            }
        };
        tabela = new JTable(modelo);
        tabela.setRowHeight(35);
        tabela.putClientProperty("JTable.autoStartsEdit", Boolean.TRUE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<6; i++) tabela.getColumnModel().getColumn(i).setCellRenderer(center);

        tabela.getColumn(LanguageManager.get("col.actions")).setCellRenderer(new ButtonsRenderer());
        tabela.getColumn(LanguageManager.get("col.actions")).setCellEditor(new ButtonsEditor(new JCheckBox()));

        corpo.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
    }

    /**
     * Busca dados no banco e preenche a tabela.
     * Constrói a query SQL dinamicamente baseada nos filtros selecionados.
     * @param filtroTexto Texto digitado pelo usuário na barra de busca.
     */
    public void carregarDados(String filtroTexto) {
        modelo.setRowCount(0);
        Connection conn = Main.Conexao.conectar();
        if(conn == null) return;
        try {
            String sql = "SELECT * FROM tab_veiculos";
            int tipoFiltro = cmbFiltro.getSelectedIndex();
            boolean temFiltro = filtroTexto != null && !filtroTexto.trim().isEmpty();

            if (temFiltro) {
                switch (tipoFiltro) {
                    case 0: sql += " WHERE marca LIKE ? OR modelo LIKE ?"; break;
                    case 1: sql += " WHERE marca LIKE ?"; break;
                    case 2: sql += " WHERE modelo LIKE ?"; break;
                    case 3:
                        if (!filtroTexto.matches("\\d+")) {
                            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error.id"));
                            return;
                        }
                        sql += " WHERE id = ?"; break;
                }
            }
            switch (tipoFiltro) {
                case 1: sql += " ORDER BY marca ASC"; break;
                case 2: sql += " ORDER BY modelo ASC"; break;
                default: sql += " ORDER BY id ASC"; break;
            }

            PreparedStatement pst = conn.prepareStatement(sql);
            if (temFiltro) {
                String buscaLike = "%" + filtroTexto + "%";
                if(tipoFiltro == 0) { pst.setString(1, buscaLike); pst.setString(2, buscaLike); }
                else if(tipoFiltro == 3) { pst.setInt(1, Integer.parseInt(filtroTexto)); }
                else { pst.setString(1, buscaLike); }
            }

            ResultSet rs = pst.executeQuery();

            while(rs.next()) {
                // 1. Cria o objeto Modelo
                Veiculo v = new Veiculo();
                v.setId(rs.getInt("id"));
                v.setMarca(rs.getString("marca"));
                v.setModelo(rs.getString("modelo"));
                v.setCor(rs.getString("cor"));
                v.setAno(rs.getInt("ano"));
                v.setQuantidade(rs.getInt("quantidade"));

                // 2. Joga na tabela via Getters
                modelo.addRow(new Object[]{
                        v.getId(),
                        v.getMarca(),
                        v.getModelo(),
                        v.getCor(),
                        v.getAno(),
                        v.getQuantidade(),
                        null
                });
            }
            conn.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void validarSoNumeros(JTextField... campos) {
        for (JTextField campo : campos) {
            campo.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) { if (!Character.isDigit(e.getKeyChar())) e.consume(); }
            });
        }
    }
    private void validarSemNumeros(JTextField... campos) {
        for (JTextField campo : campos) {
            campo.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) { if (Character.isDigit(e.getKeyChar())) e.consume(); }
            });
        }
    }

    private void abrirDialogoCadastro() {
        JTextField marca = new JTextField(); JTextField modelo = new JTextField();
        JTextField cor = new JTextField(); JTextField ano = new JTextField();
        JTextField qtd = new JTextField();

        validarSoNumeros(ano, qtd);
        validarSemNumeros(marca, cor);

        JPanel p = new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel(LanguageManager.get("col.brand"))); p.add(marca);
        p.add(new JLabel(LanguageManager.get("col.model"))); p.add(modelo);
        p.add(new JLabel(LanguageManager.get("col.color"))); p.add(cor);
        p.add(new JLabel(LanguageManager.get("col.year"))); p.add(ano);
        p.add(new JLabel(LanguageManager.get("col.qty"))); p.add(qtd);

        int r = JOptionPane.showConfirmDialog(this, p, LanguageManager.get("client.dialog.new"), JOptionPane.OK_CANCEL_OPTION);
        if(r == JOptionPane.OK_OPTION) {
            try {
                if(ano.getText().isEmpty() || qtd.getText().isEmpty()) throw new Exception(LanguageManager.get("msg.error.numeric"));

                Connection conn = Main.Conexao.conectar();
                PreparedStatement pst = conn.prepareStatement("INSERT INTO tab_veiculos (marca, modelo, cor, ano, quantidade) VALUES (?,?,?,?,?)");
                pst.setString(1, marca.getText());
                pst.setString(2, modelo.getText());
                pst.setString(3, cor.getText());
                pst.setInt(4, Integer.parseInt(ano.getText()));
                pst.setInt(5, Integer.parseInt(qtd.getText()));
                pst.executeUpdate();
                conn.close();
                carregarDados(null);
                JOptionPane.showMessageDialog(this, LanguageManager.get("msg.success"));
            } catch(Exception ex) {
                tratarErroBD(ex);
            }
        }
    }

    private void abrirDialogoEdicao(int row) {
        int idAtual = (int) tabela.getValueAt(row, 0);
        String marcaAtual = (String) tabela.getValueAt(row, 1);
        String modeloAtual = (String) tabela.getValueAt(row, 2);
        String corAtual = (String) tabela.getValueAt(row, 3);
        int anoAtual = (int) tabela.getValueAt(row, 4);
        int qtdAtual = (int) tabela.getValueAt(row, 5);

        JTextField marca = new JTextField(marcaAtual);
        JTextField modelo = new JTextField(modeloAtual);
        JTextField cor = new JTextField(corAtual);
        JTextField ano = new JTextField(String.valueOf(anoAtual));
        JTextField qtd = new JTextField(String.valueOf(qtdAtual));

        validarSoNumeros(ano, qtd);
        validarSemNumeros(marca, cor);

        JPanel p = new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel(LanguageManager.get("col.brand"))); p.add(marca);
        p.add(new JLabel(LanguageManager.get("col.model"))); p.add(modelo);
        p.add(new JLabel(LanguageManager.get("col.color"))); p.add(cor);
        p.add(new JLabel(LanguageManager.get("col.year"))); p.add(ano);
        p.add(new JLabel(LanguageManager.get("col.qty"))); p.add(qtd);

        int r = JOptionPane.showConfirmDialog(this, p, LanguageManager.get("client.dialog.edit"), JOptionPane.OK_CANCEL_OPTION);
        if(r == JOptionPane.OK_OPTION) {
            try {
                if(ano.getText().isEmpty() || qtd.getText().isEmpty()) throw new Exception(LanguageManager.get("msg.error.numeric"));

                Connection conn = Main.Conexao.conectar();
                String sql = "UPDATE tab_veiculos SET marca=?, modelo=?, cor=?, ano=?, quantidade=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, marca.getText());
                pst.setString(2, modelo.getText());
                pst.setString(3, cor.getText());
                pst.setInt(4, Integer.parseInt(ano.getText()));
                pst.setInt(5, Integer.parseInt(qtd.getText()));
                pst.setInt(6, idAtual);

                pst.executeUpdate();
                conn.close();
                carregarDados(null);
                JOptionPane.showMessageDialog(this, LanguageManager.get("msg.success"));
            } catch(Exception ex) { tratarErroBD(ex); }
        }
    }

    // Método auxiliar para tratar erros de banco
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
                int r = tabela.getSelectedRow();
                if(r >= 0) abrirDialogoEdicao(r);
                fireEditingStopped();
            });
            btnDel.addActionListener(e -> {
                int r = tabela.getSelectedRow();
                if(r >= 0) {
                    int id = (int) tabela.getValueAt(r, 0);
                    String mod = (String) tabela.getValueAt(r, 2);
                    if(JOptionPane.showConfirmDialog(null, LanguageManager.get("btn.delete") + " '" + mod + "'?", LanguageManager.get("title.attention"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            Connection conn = Main.Conexao.conectar();
                            conn.createStatement().execute("DELETE FROM tab_veiculos WHERE id="+id);
                            conn.close();
                            carregarDados(null);
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