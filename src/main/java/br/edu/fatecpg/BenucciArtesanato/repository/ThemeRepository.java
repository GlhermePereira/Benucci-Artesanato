package br.edu.fatecpg.BenucciArtesanato.repository;
import br.edu.fatecpg.BenucciArtesanato.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ThemeRepository extends JpaRepository<Theme, Long>{
Optional<Theme> findByName(String name);
}


