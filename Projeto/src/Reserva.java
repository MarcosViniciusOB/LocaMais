/**
 * Classe de modelo que representa uma Reserva (Locação) no sistema.
 * @author Marcos Vinicius
 */
public class Reserva {
    private int id_cliente, id_veiculo, id;
    private String data_reserva, data_entrega, situacao, placa;

    public Reserva(String data_entrega, String data_reserva, int id, int id_cliente, int id_veiculo, String situacao, String placa) {
        this.data_entrega = data_entrega;
        this.data_reserva = data_reserva;
        this.id = id;
        this.id_cliente = id_cliente;
        this.id_veiculo = id_veiculo;
        this.situacao = situacao;
        this.placa = placa;
    }

    public Reserva() {
        this.data_entrega = "";
        this.data_reserva = "";
        this.id = 0;
        this.id_cliente = 0;
        this.id_veiculo = 0;
        this.situacao = "";
        this.placa = "";
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getData_entrega() {
        return data_entrega;
    }

    public void setData_entrega(String data_entrega) {
        this.data_entrega = data_entrega;
    }

    public String getData_reserva() {
        return data_reserva;
    }

    public void setData_reserva(String data_reserva) {
        this.data_reserva = data_reserva;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public int getId_veiculo() {
        return id_veiculo;
    }

    public void setId_veiculo(int id_veiculo) {
        this.id_veiculo = id_veiculo;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
}
