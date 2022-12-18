package com.example.bookreviewsystem.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
