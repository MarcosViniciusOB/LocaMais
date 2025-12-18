import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Tela de visualização e histórico de Reservas.
 * Exibe a lista de locações com filtros avançados (CPF, Nome, Marca, Modelo)
 * e permite a finalização (devolução) do veículo.
 * @author Marcos Vinicius
 */
public class ReservaViewFrame extends JPanel {
    private DefaultTableModel modelo;
    private JTable tabela;
    private Main parentFrame;
    private JTextField txtBusca;

    public ReservaViewFrame(Main parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton btnVoltar = new JButton(LanguageManager.get("menu.back"));
        Main.GlobalStyles.styleButton(btnVoltar, Color.GRAY);
        btnVoltar.addActionListener(e -> parentFrame.mostrarDashboard());

        JLabel titulo = new JLabel(LanguageManager.get("reserve.history"), SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        header.add(btnVoltar, BorderLayout.WEST);
        header.add(titulo, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // --- TOOLBAR SIMPLIFICADA ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        toolbar.add(new JLabel(LanguageManager.get("client.search") + ": "));

        txtBusca = new JTextField(30);

        JButton btnBusca = new JButton(LanguageManager.get("client.search"));

        Main.GlobalStyles.styleButton(btnBusca, Main.GlobalStyles.COLOR_BLUE);
        btnBusca.setPreferredSize(new Dimension(80, 30));

        btnBusca.addActionListener(e -> carregarReservas(txtBusca.getText()));
        txtBusca.addActionListener(e -> carregarReservas(txtBusca.getText())); // Busca ao dar Enter

        toolbar.add(txtBusca);
        toolbar.add(btnBusca);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- TABELA ---
        String[] cols = {
                LanguageManager.get("col.id"),
                LanguageManager.get("col.cpf"),
                LanguageManager.get("col.name"),
                LanguageManager.get("col.brand") + " / " + LanguageManager.get("col.model"),
                LanguageManager.get("col.color"),
                LanguageManager.get("col.plate"),
                LanguageManager.get("reserve.pickup"),
                LanguageManager.get("reserve.return"),
                LanguageManager.get("reserve.status"),
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
        for (int i = 0; i < cols.length; i++) tabela.getColumnModel().getColumn(i).setCellRenderer(center);
        ((DefaultTableCellRenderer)tabela.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        TableColumn colAcao = tabela.getColumn(LanguageManager.get("col.actions"));
        colAcao.setCellRenderer(new ButtonRenderer());
        colAcao.setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    public void carregarReservas(String filtroTexto) {
        modelo.setRowCount(0);
        Connection conn = Main.Conexao.conectar();
        if(conn == null) return;
        try {
            // SQL com JOIN (Traz dados de Reserva + Cliente + Veiculo)
            String sql = "SELECT r.id, c.cpf, c.nome, v.marca, v.modelo, v.cor, r.placa, r.data_reserva, r.data_entrega, r.situacao, v.id as vid " +
                    "FROM tab_reservas r " +
                    "JOIN tab_clientes c ON r.id_cliente=c.id " +
                    "JOIN tab_veiculos v ON r.id_veiculo=v.id";

            boolean temFiltro = filtroTexto != null && !filtroTexto.trim().isEmpty();
            boolean isNumero = temFiltro && filtroTexto.matches("\\d+");

            if (temFiltro) {
                sql += " WHERE (c.nome LIKE ? OR c.cpf LIKE ? OR v.modelo LIKE ? OR v.marca LIKE ? OR v.cor LIKE ? OR r.placa LIKE ?)";
                if (isNumero) sql += " OR r.id = ?";
            }
            sql += " ORDER BY r.id DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            if (temFiltro) {
                String buscaLike = "%" + filtroTexto + "%";
                pst.setString(1, buscaLike); pst.setString(2, buscaLike);
                pst.setString(3, buscaLike); pst.setString(4, buscaLike);
                pst.setString(5, buscaLike); pst.setString(6, buscaLike);
                if (isNumero) pst.setInt(7, Integer.parseInt(filtroTexto));
            }

            ResultSet rs = pst.executeQuery();

            while(rs.next()) {
                // 1. Popula o objeto Reserva com o que é possível (dados da tabela tab_reservas)
                Reserva r = new Reserva();
                r.setId(rs.getInt("id"));
                r.setPlaca(rs.getString("placa"));
                r.setSituacao(rs.getString("situacao"));

                // Conversão de Datas
                r.setData_reserva(DateUtil.formatarData(rs.getDate("data_reserva")));
                r.setData_entrega(DateUtil.formatarData(rs.getDate("data_entrega")));


                // 2. Preenche a tabela mesclando Objeto Reserva + Dados do Join (Cliente/Veiculo)
                modelo.addRow(new Object[]{
                        r.getId(),                                      // Via Objeto
                        ValidadorCPF.formatarCPF(rs.getString("cpf")),  // Via SQL (Join)
                        rs.getString("nome"),                           // Via SQL (Join)
                        rs.getString("marca") + " / " + rs.getString("modelo"), // Via SQL
                        rs.getString("cor"),                 // Via SQL
                        r.getPlaca(),                                   // Via Objeto
                        r.getData_reserva(),                            // Via Objeto
                        r.getData_entrega(),                            // Via Objeto
                        r.getSituacao(),                                // Via Objeto
                        rs.getInt("vid")                     // Via SQL (Oculto)
                });
            }
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error") + " " + e.getMessage());
        }
    }

    // --- RENDERIZADOR DO BOTÃO ---
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        JButton btn = new JButton();
        public ButtonRenderer() {
            setOpaque(true);
            Main.GlobalStyles.styleButton(btn, Main.GlobalStyles.COLOR_GREEN);
            btn.setFont(new Font("Arial", Font.BOLD, 10));
            add(btn);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String sit = (String) table.getValueAt(row, 8); // Coluna Situação
            btn.setVisible("Utilizando".equalsIgnoreCase(sit));
            btn.setText(LanguageManager.get("btn.finish"));
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    // --- EDITOR DO BOTÃO ---
    class ButtonEditor extends DefaultCellEditor {
        JPanel p = new JPanel();
        JButton btn = new JButton();

        public ButtonEditor(JCheckBox chk) {
            super(chk);
            Main.GlobalStyles.styleButton(btn, Main.GlobalStyles.COLOR_GREEN);
            btn.setFont(new Font("Arial", Font.BOLD, 10));

            btn.addActionListener(e -> {
                int row = tabela.getSelectedRow();
                String sit = (String) tabela.getValueAt(row, 8);

                if(!"Utilizando".equalsIgnoreCase(sit)) return;

                int idRes = (int) tabela.getValueAt(row, 0);
                int idVeic = (int) tabela.getValueAt(row, 9); // Coluna oculta vid

                if(JOptionPane.showConfirmDialog(null, LanguageManager.get("reserve.dialog.confirm"), LanguageManager.get("reserve.dialog.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        Connection conn = Main.Conexao.conectar();
                        conn.setAutoCommit(false);

                        conn.createStatement().execute("UPDATE tab_reservas SET situacao='Entregue' WHERE id="+idRes);
                        conn.createStatement().execute("UPDATE tab_veiculos SET quantidade = quantidade + 1 WHERE id="+idVeic);

                        conn.commit();
                        conn.close();

                        carregarReservas(null);
                        JOptionPane.showMessageDialog(null, LanguageManager.get("reserve.success.return"));
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, LanguageManager.get("msg.error") + " " + ex.getMessage());
                    }
                }
                fireEditingStopped();
            });
            p.add(btn);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            String sit = (String) table.getValueAt(row, 8);
            btn.setVisible("Utilizando".equalsIgnoreCase(sit));
            btn.setText(LanguageManager.get("btn.finish"));
            p.setBackground(table.getSelectionBackground());
            return p;
        }
    }
}