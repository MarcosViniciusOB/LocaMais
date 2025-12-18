import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Utilitário para manipulação e conversão de datas.
 * <p>
 * Esta classe resolve o problema de incompatibilidade entre:
 * 1. String da Interface (ex: "25/12/2023")
 * 2. Objeto de Data do Java (java.util.Date)
 * 3. Objeto de Data do SQL (java.sql.Date)
 * <p>
 * Ela utiliza o {@link LanguageManager} para garantir que a data seja interpretada
 * corretamente de acordo com o país (dia antes do mês ou vice-versa).
 * @author Marcos Vinicius
 */
public class DateUtil {

    /**
     * Converte uma String digitada pelo usuário para um objeto java.sql.Date.
     * Utilizado antes de salvar no banco de dados.
     * @param dataString A data em texto (ex: "31/12/2025" ou "12/31/2025")
     * @return java.sql.Date pronto para ser inserido via PreparedStatement.
     * @throws ParseException Se o texto não for uma data válida.
     */
    public static java.sql.Date converterEValidarData(String dataString) throws ParseException {
        // Pega o formato dinâmico (dd/MM/yyyy ou MM/dd/yyyy)
        String pattern = LanguageManager.getDateFormat();
        SimpleDateFormat formato = new SimpleDateFormat(pattern);

        formato.setLenient(false);
        java.util.Date dataUtil = formato.parse(dataString);
        return new java.sql.Date(dataUtil.getTime());
    }

    /**
     * Converte uma data vinda do Banco de Dados (java.sql.Date) para String.
     * Utilizado para exibir datas nas Tabelas (JTable) e Campos de Texto.
     * @param data O objeto Date vindo do ResultSet.
     * @return String formatada conforme o idioma atual do usuário.
     */
    public static String formatarData(java.sql.Date data) {
        if (data == null) return "";
        String pattern = LanguageManager.getDateFormat();
        SimpleDateFormat formato = new SimpleDateFormat(pattern);
        return formato.format(data);
    }
}