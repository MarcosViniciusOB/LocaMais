import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Tela de cadastro de Novas Reservas.
 * <p>
 * Responsabilidades:
 * <ul>
 * <li>Buscar clientes pelo CPF.</li>
 * <li>Listar veículos disponíveis em estoque.</li>
 * <li>Calcular datas de retirada e devolução.</li>
 * <li><strong>Transação Atômica:</strong> Criar reserva e atualizar estoque do veículo.</li>
 * </ul>
 * @author Marcos Vinicius
 */
public class ReservaFrame extends JPanel {
    private JComboBox<ComboBoxItem> cmbVeiculo;
    private JTextField txtDataRetirada, txtDataDevolucao, txtCpfCliente, txtPlaca;
    private JLabel lblNomeCliente;
    private int idClienteSelecionado = -1;
    private Main parentFrame;

    public ReservaFrame(Main parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton btnVoltar = new JButton(LanguageManager.get("menu.cancel"));
        Main.GlobalStyles.styleButton(btnVoltar, Main.GlobalStyles.COLOR_RED);
        btnVoltar.addActionListener(e -> parentFrame.mostrarDashboard());

        JLabel titulo = new JLabel(LanguageManager.get("reserve.title"), SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        header.add(btnVoltar, BorderLayout.WEST);
        header.add(titulo, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        JPanel campos = new JPanel(new GridLayout(7, 2, 10, 20));
        campos.setPreferredSize(new Dimension(600, 350));
        campos.setBorder(BorderFactory.createTitledBorder(LanguageManager.get("reserve.data.title")));

        txtCpfCliente = new JTextField();
        txtCpfCliente.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String permitidos = "0123456789.-";
                if (!permitidos.contains(e.getKeyChar() + "") && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });

        JButton btnBuscaCpf = new JButton(LanguageManager.get("client.search"));
        btnBuscaCpf.addActionListener(e -> buscarCliente());
        JPanel pCpf = new JPanel(new BorderLayout());
        pCpf.add(txtCpfCliente, BorderLayout.CENTER);
        pCpf.add(btnBuscaCpf, BorderLayout.EAST);

        campos.add(new JLabel(LanguageManager.get("col.cpf") + ":"));
        campos.add(pCpf);

        campos.add(new JLabel(LanguageManager.get("col.name") + ":"));
        lblNomeCliente = new JLabel("---");
        lblNomeCliente.setForeground(Color.BLUE);
        campos.add(lblNomeCliente);

        campos.add(new JLabel(LanguageManager.get("menu.vehicle") + ":"));
        cmbVeiculo = new JComboBox<>();
        campos.add(cmbVeiculo);

        campos.add(new JLabel(LanguageManager.get("col.plate") + ":"));
        txtPlaca = new JTextField();
        campos.add(txtPlaca);

        String formatoData = LanguageManager.getDateFormat();
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatoData);

        campos.add(new JLabel(LanguageManager.get("reserve.pickup") + " (" + formatoData + "):"));
        txtDataRetirada = new JTextField(dateFormat.format(new java.util.Date()));
        txtDataRetirada.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String permitidos = "0123456789/";
                if (!permitidos.contains(e.getKeyChar() + "") && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) e.consume();
            }
        });
        campos.add(txtDataRetirada);

        campos.add(new JLabel(LanguageManager.get("reserve.return") + " (" + formatoData + "):"));
        Calendar c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, 7);
        txtDataDevolucao = new JTextField(dateFormat.format(c.getTime()));
        txtDataDevolucao.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String permitidos = "0123456789/";
                if (!permitidos.contains(e.getKeyChar() + "") && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) e.consume();
            }
        });
        campos.add(txtDataDevolucao);

        form.add(campos);
        add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton btnSalvar = new JButton(LanguageManager.get("btn.confirm"));
        Main.GlobalStyles.styleButton(btnSalvar, Main.GlobalStyles.COLOR_GREEN);
        btnSalvar.setPreferredSize(new Dimension(250, 50));
        btnSalvar.addActionListener(e -> salvarReserva());

        footer.add(btnSalvar);
        footer.setBorder(BorderFactory.createEmptyBorder(0,0,30,0));
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Reseta o formulário para o estado inicial.
     * Busca no banco apenas veículos com quantidade > 0.
     */
    public void atualizarDadosIniciais() {
        cmbVeiculo.removeAllItems();
        txtCpfCliente.setText("");
        lblNomeCliente.setText("---");
        txtPlaca.setText("");
        idClienteSelecionado = -1;

        SimpleDateFormat dateFormat = new SimpleDateFormat(LanguageManager.getDateFormat());
        txtDataRetirada.setText(dateFormat.format(new java.util.Date()));
        Calendar c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, 7);
        txtDataDevolucao.setText(dateFormat.format(c.getTime()));

        Connection conn = Main.Conexao.conectar();
        if(conn == null) return;
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT id, marca, modelo, cor FROM tab_veiculos WHERE quantidade > 0");
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                String descricao = rs.getString("marca") + " - " + rs.getString("modelo") + " (" + rs.getString("cor") + ")";
                cmbVeiculo.addItem(new ComboBoxItem(rs.getInt("id"), descricao));
            }
            conn.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Busca um cliente no banco baseado no CPF digitado.
     * Atualiza o label com o nome ou mensagem de erro.
     */
    private void buscarCliente() {
        if (!ValidadorCPF.isCPF(txtCpfCliente.getText())) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error.cpf"), LanguageManager.get("title.error"), JOptionPane.ERROR_MESSAGE);
            txtCpfCliente.requestFocus();
            txtCpfCliente.selectAll();
            return;
        }

        Connection conn = Main.Conexao.conectar();
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT id, nome FROM tab_clientes WHERE cpf = ?");
            pst.setString(1, txtCpfCliente.getText());
            ResultSet rs = pst.executeQuery();
            if(rs.next()) {
                idClienteSelecionado = rs.getInt("id");
                lblNomeCliente.setText(rs.getString("nome"));
            } else {
                lblNomeCliente.setText(LanguageManager.get("reserve.client.notfound"));
                idClienteSelecionado = -1;
            }
            conn.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Executa a lógica principal de salvamento.
     */
    private void salvarReserva() {
        if(idClienteSelecionado == -1 || cmbVeiculo.getSelectedItem() == null || txtPlaca.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error.empty"));
            return;
        }

        try {
            Connection conn = Main.Conexao.conectar();
            conn.setAutoCommit(false);

            String sqlRes = "INSERT INTO tab_reservas (id_veiculo, id_cliente, data_reserva, data_entrega, situacao, placa) VALUES (?,?,?,?,?,?)";
            PreparedStatement pst = conn.prepareStatement(sqlRes);
            pst.setInt(1, ((ComboBoxItem)cmbVeiculo.getSelectedItem()).getId());
            pst.setInt(2, idClienteSelecionado);
            pst.setDate(3, DateUtil.converterEValidarData(txtDataRetirada.getText()));
            pst.setDate(4, DateUtil.converterEValidarData(txtDataDevolucao.getText()));
            pst.setString(5, "Utilizando");
            pst.setString(6, txtPlaca.getText().toUpperCase());
            pst.executeUpdate();

            PreparedStatement pstUpd = conn.prepareStatement("UPDATE tab_veiculos SET quantidade = quantidade - 1 WHERE id = ?");
            pstUpd.setInt(1, ((ComboBoxItem)cmbVeiculo.getSelectedItem()).getId());
            pstUpd.executeUpdate();

            conn.commit();
            conn.close();

            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.success"));
            parentFrame.mostrarDashboard();

        } catch(Exception e) { JOptionPane.showMessageDialog(this, LanguageManager.get("msg.error") + " " + e.getMessage()); }
    }
}