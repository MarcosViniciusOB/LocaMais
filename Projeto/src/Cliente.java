/**
 * Classe de modelo que representa um Cliente no sistema.
 * @author Marcos Vinicius
 */
public class Cliente {
    private String nome, cpf, data_nasc;
    private int id;

    public Cliente(String cpf, String nome, int id, String data_nasc) {
        this.cpf = cpf;
        this.nome = nome;
        this.id = id;
        this.data_nasc = data_nasc;
    }

    public Cliente() {
        this.cpf = "";
        this.nome = "";
        this.id = 0;
        this.data_nasc = "";
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData_nasc() {
        return data_nasc;
    }

    public void setData_nasc(String data_nasc) {
        this.data_nasc = data_nasc;
    }
}
