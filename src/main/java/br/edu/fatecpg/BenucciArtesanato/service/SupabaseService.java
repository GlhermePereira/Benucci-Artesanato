package br.edu.fatecpg.BenucciArtesanato.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Faz upload de um arquivo para o Supabase Storage usando service role key.
     * Detecta automaticamente o Content-Type, gera nome único e retorna URL pública.
     */
    public String uploadImage(String originalFileName, byte[] imageBytes) {
        // Gera nome único com UUID + timestamp + extensão
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID() + "_" + Instant.now().toEpochMilli() + extension;
        String safeFileName = URLEncoder.encode(uniqueFileName, StandardCharsets.UTF_8);

        String url = normalizeBaseUrl(supabaseUrl) + "/storage/v1/object/" + supabaseBucket + "/" + safeFileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeByExtension(extension));
        headers.set("apikey", supabaseServiceRoleKey);
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return safeFileName;
            }
        };

        HttpEntity<ByteArrayResource> request = new HttpEntity<>(fileResource, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Erro ao fazer upload: HTTP " + response.getStatusCodeValue() + " - " + response.getBody());
            }

            return normalizeBaseUrl(supabaseUrl) + "/storage/v1/object/public/" + supabaseBucket + "/" + safeFileName;

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            throw new RuntimeException("Erro uploading image to Supabase: HTTP " + e.getRawStatusCode() + " - " + responseBody, e);
        } catch (Exception e) {
            throw new RuntimeException("Erro uploading image to Supabase", e);
        }
    }

    private String normalizeBaseUrl(String base) {
        if (base.endsWith("/")) return base.substring(0, base.length() - 1);
        return base;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex != -1) ? fileName.substring(dotIndex) : "";
    }

    private MediaType getMediaTypeByExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case ".png" -> MediaType.IMAGE_PNG;
            case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG;
            case ".gif" -> MediaType.IMAGE_GIF;
            case ".webp" -> MediaType.valueOf("image/webp");
            case ".pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
