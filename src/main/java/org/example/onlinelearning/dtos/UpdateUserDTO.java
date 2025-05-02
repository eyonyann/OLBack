package org.example.onlinelearning.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {
    private Long id;

    private String fullname;
    private String username;
    private String password;
    private String newPassword;

}
