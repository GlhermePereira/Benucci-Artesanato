package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;

import java.util.Optional;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        if (dto == null) return null;

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // set optional fields if present
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        // map incoming DTO.type to security role field
        if (dto.getType() != null) {
            String t = dto.getType().toLowerCase();
            if (t.contains("admin")) user.setRole("ROLE_ADMIN");
            else user.setRole("ROLE_USER");
        } else {
            user.setRole("ROLE_USER");
        }
        user.setCpf(dto.getCpf());

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin) {
            if (dto.getType() != null && !dto.getType().isBlank()) {
                throw new AccessDeniedException("Você não pode alterar o tipo de usuário.");
            }

            String requesterEmail = authentication.getName();
            Optional<User> requesterOpt = userRepository.findByEmail(requesterEmail);

            if (requesterOpt.isEmpty() || !requesterOpt.get().getId().equals(id)) {
                throw new AccessDeniedException("Você não tem permissão para atualizar este usuário.");
            }
        }

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (dto.getName() != null) user.setName(dto.getName());
            if (dto.getEmail() != null) user.setEmail(dto.getEmail());
            if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
            if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getAddress() != null) user.setAddress(dto.getAddress());
            if (dto.getType() != null) {
                String t = dto.getType().toLowerCase();
                if (t.contains("admin")) user.setRole("ROLE_ADMIN");
                else user.setRole("ROLE_USER");
            }
            if (dto.getCpf() != null) user.setCpf(dto.getCpf());

            return userRepository.save(user);
        }
        return null;
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