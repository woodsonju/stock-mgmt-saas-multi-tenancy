package com.woodev.saas.responses;

import com.woodev.saas.entities.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String username;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private UserRole role;
}
