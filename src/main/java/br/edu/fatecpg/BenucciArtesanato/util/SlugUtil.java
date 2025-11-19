package br.edu.fatecpg.BenucciArtesanato.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilitário para gerar slugs (URL amigável) a partir de um nome de string.
 */
public class SlugUtil {

    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^\\w\\s-]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s]");

    /**
     * Converte uma string (ex: "Bolsa de Mão Ceará") em um slug (ex: "bolsa-de-mao-ceara").
     * @param input A string de entrada.
     * @return O slug gerado.
     */
    public static String generateSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 1. Normaliza a string (remove acentos e caracteres especiais do Português)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String nonAccented = NON_ALPHANUMERIC_PATTERN.matcher(normalized).replaceAll("");

        // 2. Converte para minúsculas
        String lowerCase = nonAccented.toLowerCase(Locale.ROOT);

        // 3. Substitui espaços e outros separadores por hífens
        String slug = WHITESPACE_PATTERN.matcher(lowerCase).replaceAll("-");

        // 4. Remove múltiplos hífens seguidos e hífens nas extremidades
        slug = slug.replaceAll("(-){2,}", "-");
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}