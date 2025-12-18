/**
 * Classe auxiliar para armazenar objetos dentro de um JComboBox do Swing.
 * <p>
 * O JComboBox padrão exibe o resultado do método toString().
 * Esta classe permite exibir uma descrição amigável (ex: "Ford Ka - Branco")
 * enquanto armazena o ID do banco de dados internamente para uso lógico.
 */
public class ComboBoxItem {
    private int id;
    private String descricao;
    private String cpf;

    public ComboBoxItem(int id, String descricao) {
        this(id, descricao, null);
    }

    public ComboBoxItem(int id, String descricao, String cpf) {
        this.id = id;
        this.descricao = descricao;
        this.cpf = cpf;
    }

    public int getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    @Override
    public String toString() {
        return descricao;
    }
}