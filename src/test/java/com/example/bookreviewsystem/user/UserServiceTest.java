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

    /*
    this test shows how to use mockito to test classes that use static methods like LocalDateTime, UUID, etc...
     */

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService cut;

    @Test
    void shouldIncludeCurrentDateTimeWhenCreatingNewUser() {
        // Given
        given(userRepository.findByNameAndEmail("duke", "duke@spring.io")).willReturn(Optional.empty());
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        LocalDateTime defaultLocalDateTime = LocalDateTime.of(2022, 12, 13, 12, 15);

        /*
        the mocked version of LocalDateTime is only available within the try with resources block. Outside the try with resources
        statement we will have the normal behavior of LocalDateTime
         */
        try(MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(defaultLocalDateTime);

            UserDTO result = cut.getOrCreateUser("duke", "duke@spring.io");

            assertThat(result.createdAt()).isEqualTo(defaultLocalDateTime);
        }

    }


}