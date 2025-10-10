package br.edu.fatecpg.BenucciArtesanato.record.dto;

import br.edu.fatecpg.BenucciArtesanato.model.User;
import lombok.Data;

@Data
public class UserDTO {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String type;
    private String cpf;

    // ✅ método declarado como static e DENTRO da classe
    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setType(user.getType());
        dto.setCpf(user.getCpf());
        return dto;
    }
}
