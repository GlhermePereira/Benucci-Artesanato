package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.exception.InvalidDataException;
import br.edu.fatecpg.BenucciArtesanato.exception.ResourceNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Converte "admin" ou "ADMIN" → ROLE_ADMIN, senão ROLE_USER */
    private String mapRole(String rawRole) {
        if (rawRole == null) return "ROLE_USER";
        return rawRole.toLowerCase().contains("admin") ? "ROLE_ADMIN" : "ROLE_USER";
    }

    public User searchByEmail(String email) {
           return userRepository.findByEmail(email)
                  .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para email: " + email));
    }

    // List all users
    public List<User> getAll() {
        return userRepository.findAll();
    }

    // Get by id
    public User getById(Long id) {
      return userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
    }

    // Register new user
    public User registerUser(UserDTO dto) {
        if (dto == null) throw new InvalidDataException("DTO não pode ser nulo");
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new InvalidDataException("Email já cadastrado: " + dto.getEmail());
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

    // Update user
    public User updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));

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
    /** Deleta usuário ou lança exceção */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User getByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    public User getByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com email: " + email));
    }

    public void deleteUserOrThrow(Long id) {
        User user = getByIdOrThrow(id);
        userRepository.delete(user);
    }
}