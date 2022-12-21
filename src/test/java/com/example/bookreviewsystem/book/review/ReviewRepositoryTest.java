package com.example.bookreviewsystem.book.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver",
        "DB_CLOSE_DELAY=-1",
        "DB_CLOSE_ON_EXIT=false"

})

@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Container
    static PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:12.3")
            .withDatabaseName("test")
            .withUsername("duke")
            .withPassword("s3cret");
    /*
  withReuse setting is used to reuse containers across tests. Meaning, container does not stop after
  tests finish executing. However, @TestContainer and @Container annotations cannot be used, therefore
  the lifecycle of the testcontainers also needs to be done manually either with static method or @BeforeAll
   */
//    .withReuse(true);

    // manually manage the life cycle of container, choose when to start the test containers.
    // the test containers must start before the application context is created otherwise it fails.
    // test containers could also be started from @BeforeAll Junit5 test method
    // need to reomve @Container and @TestContainers annotations for this to work
//  static {
//    container.start();
//  }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:p6spy:postgresql://" + sqlContainer.getHost() + ":" +
                sqlContainer.getFirstMappedPort() + "/" + sqlContainer.getDatabaseName());
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
    }

    @Autowired
    private ReviewRepository cut;

    @Test
    @Sql(scripts = "/scripts/INIT_REVIEW_EACH_BOOK.sql") // changes by Sql are rolled back after test
    void shouldGetTwoReviewStatisticsWhenDatabaseContainsTwoBooksWithReview() {
        // When -> data provided through sql script file

        // When
        List<ReviewStatistic> result = cut.getReviewStatistics();

        // Then
        assertThat(cut.count()).isEqualTo(3);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getRatings()).isEqualTo(2);
        assertThat(result.get(0).getId()).isEqualTo(2);
    }
}