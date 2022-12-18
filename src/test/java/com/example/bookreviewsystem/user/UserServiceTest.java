package com.example.bookreviewsystem.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService cut;

    @Test
    void shouldIncludeCurrentDateTimeWhenCreatingNewUser() {
       // Given
        String name = "duke";
        String email = "duke@spring.io";
       given(userRepository.findByNameAndEmail(name, email)).willReturn(Optional.empty());
       given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> {
           UserEntity userEntity = invocation.getArgument(0);
           userEntity.setId(1L);
           return userEntity;
       });

        LocalDateTime defaultLocalDateTime = LocalDateTime.of(2022, 12, 13, 12, 15);

        /*
        the mocked version of LocalDateTime is only available within the try with resources statement.
        Outside of it, we still have the normal behavior of LocalDateTime
         */
        try(MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(defaultLocalDateTime);

            UserDTO result = cut.getOrCreateUser(name, email);
            assertThat(result.getCreatedAt()).isEqualTo(defaultLocalDateTime);
        }
    }
}