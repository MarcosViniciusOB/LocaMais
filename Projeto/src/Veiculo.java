/**
 * Classe de modelo que representa um Veículo no sistema.
 * Contém os atributos básicos como marca, modelo, cor, ano e quantidade em estoque.
 * @author Marcos Vinicius
 */
public class Veiculo {
    private String marca, modelo, cor;
    private int ano, quantidade, id;

    public Veiculo(int ano, int id, String marca, String modelo, String cor, int quantidade) {
        this.ano = ano;
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.cor = cor;
        this.quantidade = quantidade;
    }

    public Veiculo() {
        this.ano = 0;
        this.id = 0;
        this.marca = "";
        this.modelo = "";
        this.cor = "";
        this.quantidade = 0;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
