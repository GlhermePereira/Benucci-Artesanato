package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Theme;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ThemeInputDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.ThemeRepository;
import br.edu.fatecpg.BenucciArtesanato.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    // =============================
    // Criar Tema
    // =============================
    public ThemeInputDTO createTheme(ThemeInputDTO dto) {
        if (themeRepository.existsByName(dto.getName())) {
            throw new IllegalStateException("Já existe um tema com esse nome.");
        }

        Theme theme = Theme.builder()
                .name(dto.getName())
                .slug(SlugUtil.generateSlug(dto.getName()))
                .description(dto.getDescription())
                .build();

        Theme saved = themeRepository.save(theme);
        return mapToDto(saved);
    }

    // =============================
    // Atualizar Tema
    // =============================
    public ThemeInputDTO updateTheme(Long id, ThemeInputDTO dto) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tema não encontrado"));

        if (!theme.getName().equals(dto.getName()) && themeRepository.existsByName(dto.getName())) {
            throw new IllegalStateException("Já existe outro tema com esse nome.");
        }

        theme.setName(dto.getName());
        theme.setSlug(SlugUtil.generateSlug(dto.getName()));
        theme.setDescription(dto.getDescription());

        Theme updated = themeRepository.save(theme);
        return mapToDto(updated);
    }

    // =============================
    // Deletar Tema
    // =============================
    public void deleteTheme(Long id) {
        if (!themeRepository.existsById(id)) {
            throw new RuntimeException("Tema não encontrado");
        }
        themeRepository.deleteById(id);
    }

    // =============================
    // Listar Todos os Temas
    // =============================
    public List<ThemeInputDTO> getAllThemes() {
        return themeRepository.findAllByOrderByNameAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    // =============================
    // Mapper
    // =============================
    private ThemeInputDTO mapToDto(Theme theme) {
        return ThemeInputDTO.builder()
                .id(theme.getId())
                .name(theme.getName())
                .slug(theme.getSlug())
                .description(theme.getDescription())
                .build();
    }
}
