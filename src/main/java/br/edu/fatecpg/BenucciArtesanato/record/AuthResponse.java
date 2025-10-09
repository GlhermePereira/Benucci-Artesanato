package br.edu.fatecpg.BenucciArtesanato.record;

import br.edu.fatecpg.BenucciArtesanato.model.User;

public record AuthResponse(String token, User user) {}