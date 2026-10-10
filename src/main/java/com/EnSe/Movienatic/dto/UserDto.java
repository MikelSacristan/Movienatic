package com.EnSe.Movienatic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// DTO con validaciones para la creación de un usuario
public record UserDto(@NotBlank String username, @NotBlank @Email String email,
                @NotBlank String password) {

}
/*package com.EnSe.Movienatic.dto;

import com.fasterxml.jackson.annotation.JsonView;

public record UserDto (
        @JsonView(Views.Public.class) String username,
        @JsonView(Views.Public.class) String email,
        @JsonView(Views.Public.class) String password
){
    public static UserDto from(com.EnSe.Movienatic.model.User user) {
        return new UserDto(user.getUsername(), user.getEmail(), user.getPassword());
    }

    public interface Views {
        interface Public {}
        interface Private extends Public {}
    }
}*/