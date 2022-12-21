package com.example.bookreviewsystem.user;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getOrCreateUser(String name, String email) {
        Optional<UserEntity> userOptional = userRepository.findByNameAndEmail(name, email);

        if (userOptional.isPresent()) return userOptional.get();

        UserEntity userEntity = UserEntity.builder()
                .name(name)
                .email(email)
                .createdAt(LocalDateTime.now())
                .build();

        userEntity = userRepository.save(userEntity);

        return userEntity;
    }

//    public UserDTO getOrCreateUser(String name, String email) {
//        Optional<UserEntity> userOptional = userRepository.findByNameAndEmail(name, email);
//
//        if (userOptional.isPresent()) {
//            return convertUserEntity(userOptional.get());
//        }
//
//        UserEntity userEntity = UserEntity.builder()
//                .name(name)
//                .email(email)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        userEntity = userRepository.save(userEntity);
//
//        return convertUserEntity(userEntity);
//
//    }
//
//    private UserDTO convertUserEntity(UserEntity userEntity) {
//        return new UserDTO(userEntity.getName(), userEntity.getEmail(), userEntity.getCreatedAt());
//    }
}
