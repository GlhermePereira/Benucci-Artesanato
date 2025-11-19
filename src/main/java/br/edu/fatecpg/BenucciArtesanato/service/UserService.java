package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;

import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    /** Converte "admin" ou "ADMIN" → ROLE_ADMIN, senão ROLE_USER */
    private String mapRole(String rawRole) {
        if (rawRole == null) return "ROLE_USER";

        String r = rawRole.toLowerCase();
        return r.contains("admin") ? "ROLE_ADMIN" : "ROLE_USER";
    }

    public User searchByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    // List all users
    public List<User> getAll() {
        return userRepository.findAll();
    }

    // Get by id
    public User getById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    // Register new user
    public User registerUser(UserDTO dto) {
        if (dto == null) throw new IllegalArgumentException("DTO não pode ser nulo");

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .cpf(dto.getCpf())
                .role(mapRole(dto.getRole()))
                .build();

        return userRepository.save(user);
    }

    // Authenticate
    public User autenticar(String email, String senha) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(senha, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    // Update user
    public User updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null)
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getRole() != null) user.setRole(mapRole(dto.getRole()));
        if (dto.getCpf() != null) user.setCpf(dto.getCpf());

        return userRepository.save(user);
    }


    // Delete user
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}