package com.example.bookreviewsystem.review;

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

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/*
@AutoConfigureTestDatabase is a Spring Boot Test annotation to trigger the auto-configuration of a database
for testing purposes. By default, this would override the application-specific database and use an
embedded database like H2.

basically don't create the embedded testing database like h2 on its own
instead use the database specific in spring.datasource.url by using Replace.NONE
 */

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver",
        "DB_CLOSE_DELAY=-1",
        "DB_CLOSE_ON_EXIT=false"
})
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    /*
    withReuse setting is used to reuse containers across tests. Meaning that the container does not stop after
    tests finish executing. However, @TestContainer and @Container annotations cannot be used, therefore the
    lifecycle of the testcontainers also needs to be managed manually either with static method(static class initializer)
    or @BeforeAll

    static { dbContainer.start() } or inside @BeforeAll lifecycle method
     */

    @Container
    static PostgreSQLContainer<?> dbContainer = new PostgreSQLContainer<>("postgres:12.3")
            .withDatabaseName("test")
            .withUsername("duke")
            .withPassword("s3cret");
//            .withReuse(true)

    // p6spy proxy applied to get better logging for DB debugging
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:p6spy:postgresql://" + dbContainer.getHost() + ":" +
                      dbContainer.getFirstMappedPort() + "/" + dbContainer.getDatabaseName()
        );
//        registry.add("spring.datasource.url", dbContainer::getJdbcUrl);
        registry.add("spring.datasource.password", dbContainer::getPassword);
        registry.add("spring.datasource.username", dbContainer::getUsername);
    }

    @Autowired
    private ReviewRepository cut;


    /*
    The initial data needed to execute this test can be done programmatically or using @Sql tag.

    Using @Sql tag can be hard to maintain if application is in a state where the structure is constantly changing
    because the scripts would need to be updated to match the db structure

    Another alternative is to export a snapshot of the production database data to a development/testing database
    where tests can be executed against

    We can also provide a test container with a custom created postgres db that is already pre-populated with data
    from a data snapchat of the production database. Tests can then be tested against this db test container.
     */
    @Test
    @Sql(scripts = "/scripts/INIT_REVIEW_EACH_BOOK.sql") // changes by Sql are rolled back after test
    void shouldGetTwoReviewStatisticsWhenDatabaseContainsTwoBooksWithReview() throws SQLException {
        // Given -> data provided through sql file

        // When
        List<ReviewStatistic> result = cut.getReviewStatistics();
        System.out.println(dbContainer.getJdbcUrl());
        System.out.println(dbContainer.getFirstMappedPort());

        // Then
        assertThat(cut.count()).isEqualTo(3);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getRatings()).isEqualTo(2);
        assertThat(result.get(0).getId()).isEqualTo(2);
    }
}