package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import br.edu.fatecpg.BenucciArtesanato.model.Theme;
import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryThemeAssignDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.SubcategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.SubcategoryThemeRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ThemeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubcategoryThemeService {

    private final SubcategoryRepository subcategoryRepository;
    private final ThemeRepository themeRepository;
    private final SubcategoryThemeRepository subcategoryThemeRepository;

    // =============================
    // Atribuir/Associar temas (POST)
    // =============================
    @Transactional
    public void assignThemesToSubcategory(SubcategoryThemeAssignDTO dto) {
        SubCategory sub = subcategoryRepository.findById(dto.getSubcategoryId())
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        if (!sub.getCategory().getId().equals(dto.getCategoryId())) {
            throw new RuntimeException("Subcategoria não pertence à categoria informada");
        }

        // Buscar temas existentes
        List<Theme> existingThemes = sub.getThemes();

        // Buscar novos temas a adicionar
        List<Theme> newThemes = themeRepository.findAllById(dto.getThemeIds());

        // Adicionar apenas os que não existem ainda
        for (Theme theme : newThemes) {
            if (!existingThemes.contains(theme)) {
                existingThemes.add(theme);
            }
        }

        subcategoryRepository.save(sub);
    }


    // =============================
    // Atualizar/Substituir temas (PUT)
    // =============================
    @Transactional
    public void updateThemes(Long subcategoryId, List<Long> themeIds) {
        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        List<Theme> themes = themeRepository.findAllById(themeIds);
        sub.setThemes(themes); // substitui os temas existentes
        subcategoryRepository.save(sub);
    }

    // =============================
    // Remover associação específica (DELETE)
    // =============================
    @Transactional
    public void removeThemeFromSubcategory(Long subcategoryId, Long themeId) {
        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Tema não encontrado"));

        sub.getThemes().remove(theme);
        subcategoryRepository.save(sub);
    }

    // =============================
    // Listar IDs de temas (GET)
    // =============================
    public List<Long> getThemesBySubcategory(Long subcategoryId) {
        return subcategoryThemeRepository.findThemeIdsBySubcategoryId(subcategoryId);
    }
}
