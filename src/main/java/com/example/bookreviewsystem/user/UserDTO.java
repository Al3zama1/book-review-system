package com.example.bookreviewsystem.user;

import java.time.LocalDateTime;

public record UserDTO(String name, String email, LocalDateTime createdAt) {

}
