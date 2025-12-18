/**
 * Classe utilitária para validação e formatação de CPF.
 * <p>
 * Implementa o algoritmo oficial do Ministério da Fazenda para verificação
 * dos dígitos verificadores.
 * @author Marcos Vinicius
 */
public class ValidadorCPF {

    /**
     * Verifica se um CPF é válido.
     * @param cpf A string contendo o CPF (com ou sem pontuação).
     * @return true se o CPF for válido, false caso contrário.
     */
    public static boolean isCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int sm = 0, peso = 10;
            for (int i = 0; i < 9; i++) { sm += (cpf.charAt(i) - 48) * peso--; }
            int r = 11 - (sm % 11);
            char dig10 = (r == 10 || r == 11) ? '0' : (char) (r + 48);

            sm = 0; peso = 11;
            for (int i = 0; i < 10; i++) { sm += (cpf.charAt(i) - 48) * peso--; }
            r = 11 - (sm % 11);
            char dig11 = (r == 10 || r == 11) ? '0' : (char) (r + 48);

            return (dig10 == cpf.charAt(9)) && (dig11 == cpf.charAt(10));
        } catch (Exception e) { return false; }
    }

    /**
     * Formata uma string de 11 números para o padrão visual de CPF.
     * @param cpf String numérica.
     * @return String formatada (ex: 123.456.789-00) ou o original se inválido.
     */
    public static String formatarCPF(String cpf) {
        if (cpf == null) return "";

        // Remove tudo que não é número para garantir
        String limpo = cpf.replaceAll("[^0-9]", "");

        // Se não tiver 11 dígitos, retorna como está (evita erro)
        if (limpo.length() != 11) {
            return cpf;
        }

        // Aplica a máscara XXX.XXX.XXX-XX
        return limpo.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}