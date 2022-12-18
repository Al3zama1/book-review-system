package com.example.bookreviewsystem.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getOrCreateUser(String name, String email) {
        Optional<UserEntity> userOptional = userRepository.findByNameAndEmail(name, email);
        if (userOptional.isPresent()) return convertUser(userOptional.get());

        UserEntity userEntity = UserEntity.builder()
                .name(name)
                .email(email)
                .createdAt(LocalDateTime.now()).build();

        UserEntity savedUser = userRepository.save(userEntity);

        return convertUser(savedUser);
    }

    private UserDTO convertUser(UserEntity userEntity) {
        return new UserDTO(userEntity.getName(), userEntity.getEmail(), userEntity.getCreatedAt());
    }
}
