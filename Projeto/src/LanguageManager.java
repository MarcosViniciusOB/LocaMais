import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Gerencia a internacionalização (i18n) da aplicação Loca Mais.
 * <p>
 * Esta classe é responsável por:
 * <ul>
 * <li>Manter o estado do idioma atual (Locale) selecionado pelo usuário.</li>
 * <li>Carregar o arquivo .properties correto (messages_pt_BR, messages_en_US, etc).</li>
 * <li>Fornecer as strings traduzidas para a interface.</li>
 * <li>Determinar o formato de data (dd/MM vs MM/dd) baseado na cultura.</li>
 * </ul>
 * @author Marcos Vinicius
 */
public class LanguageManager {
    private static Locale currentLocale = new Locale("pt", "BR");
    private static ResourceBundle messages = ResourceBundle.getBundle("messages", currentLocale);

    /**
     * Altera o idioma da aplicação em tempo de execução.
     * Após chamar este método, a interface deve ser recarregada para surtir efeito.
     * @param language Código do idioma (ex: "en", "pt", "es")
     * @param country Código do país (ex: "US", "BR", "ES")
     */
    public static void setLanguage(String language, String country) {
        currentLocale = new Locale(language, country);
        messages = ResourceBundle.getBundle("messages", currentLocale);
    }

    /**
     * Retorna uma string traduzida baseada em uma chave.
     * @param key A chave definida no arquivo .properties (ex: "menu.exit")
     * @return O texto traduzido ou a própria chave entre "???" caso não encontre.
     */
    public static String get(String key) {
        try {
            return messages.getString(key);
        } catch (Exception e) {
            return "???" + key + "???";
        }
    }

    /**
     * Retorna o Locale atualmente configurado.
     * Útil para componentes que precisam saber se é EN, PT, etc.
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * Retorna o padrão de formatação de data baseado na cultura do idioma atual.
     * <p>
     * Lógica:
     * <ul>
     * <li>Se for Inglês (en): Retorna "MM/dd/yyyy" (Mês primeiro).</li>
     * <li>Qualquer outro: Retorna "dd/MM/yyyy" (Dia primeiro).</li>
     * </ul>
     * @return String contendo o pattern de data.
     */
    public static String getDateFormat() {
        if (currentLocale.getLanguage().equals("en")) {
            return "MM/dd/yyyy";
        }
        return "dd/MM/yyyy";
    }
}