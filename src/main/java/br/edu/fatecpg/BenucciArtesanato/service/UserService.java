package br.edu.fatecpg.BenucciArtesanato.service;


import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }// Adicione na UserService
    public User searchByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    public User updateUser(Long id, User updateUser) {
        return userRepository.findById(id)
                    .map(user -> {
                        user.setName(updateUser.getName());
                        user.setEmail(updateUser.getEmail());
                        user.setPhoneNumber(updateUser.getPhoneNumber());
                        user.setAddress(updateUser.getAddress());
                        user.setType(updateUser.getType());
                        return userRepository.save(user);
                    }).orElseThrow(() -> new RuntimeException("User not found"));

    }

    // Cadastrar novo usuário
    public User registerUser(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // hash da password
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setType(dto.getType());
        return userRepository.save(user);
    }

    // Autenticar usuário
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

    // Buscar por ID
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Atualizar usuário
    public User updateUser(Long id, UserDTO dto) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setAddress(dto.getAddress());
        user.setType(dto.getType());
        return userRepository.save(user);
    }

    // Remover usuário
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }

}
