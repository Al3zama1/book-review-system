package com.example.bookreviewsystem;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.book.review.ReviewRepository;
import com.example.bookreviewsystem.book.stubs.OAuth2Stubs;
import com.example.bookreviewsystem.book.stubs.OpenLibraryStubs;
import com.example.bookreviewsystem.initializer.DefaultBookStubsInitializer;
import com.example.bookreviewsystem.initializer.RSAKeyGenerator;
import com.example.bookreviewsystem.initializer.WireMockInitializer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

/*
WAYS TO MOVE LOGIN OUT OF INTEGRATION TESTS TO A CENTRAL PLACE TO MAKE INTEGRATION TESTS CLEANER (LESS POLLUTED)
- AbstractIntegrationTest (this method is used here)
- Multiple Initializers
- Write our own custom annotation

One pro of using an abstractIntegrationTest is that we can have a contract  that all integration tests that
extend it can utilize the same spring context, which makes the integration tests faster because each one
will not have to start its own spring context
 */

/*
to make sure we have a keycloack locally running to carry out our integration tests there is multiple options.
- we could actually use a docker container where keycloack is running and prepopulate the keycloack configuration
like how we are doing it inside the docker-compose file.

- we could also use our whole docker-compose infrastructure to start the whole local environment
we need for our application.

- As there is only http communication happening between keycloack and our application we can mock all these HTTP calls.
One possible Http simulator option for doing this is to use Wiremock. This option is being used here.

    this time we are using a contextInitializer for wiremockInitializer, but we could also use the Wiremock extension for
    JUnit5.
 */
@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = {WireMockInitializer.class, DefaultBookStubsInitializer.class})
/*
it will start the spring context without the embedded servlet container.
therefore we won't actually occupy a port on the system.

webEnvironment is needed to also start a port. This is important if we want to
write tests with real http interaction from the test to a locally running
application
 */

// we want real http communication to our application
// it will start the embedded tomcat servlet container
/*
spring boot auto populates a http client, there is both the WebTestClient and
the RestTestTemplate. webEnvironment is required for this
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractIntegrationTest {

    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:12.3")
            .withDatabaseName("test")
            .withUsername("duke")
            .withPassword("s3cret");

    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:0.13.3"))
            .withServices(LocalStackContainer.Service.SQS);
    // can be removed with version 0.12.17 as LocalStack now has multi-region support https://docs.localstack.cloud/localstack/configuration/#deprecated
    // .withEnv("DEFAULT_REGION", "eu-central-1");

    static {
        database.start();
        localStack.start();
    }

    protected static final String QUEUE_NAME = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("sqs.book-synchronization-queue", () -> QUEUE_NAME);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AmazonSQSAsync amazonSQSAsync() {
            return AmazonSQSAsyncClientBuilder.standard()
                    .withCredentials(localStack.getDefaultCredentialsProvider())
                    .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
                    .build();
        }
    }

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RSAKeyGenerator rsaKeyGenerator;

    @Autowired
    private OAuth2Stubs oAuth2Stubs;

    @Autowired
    protected OpenLibraryStubs openLibraryStubs;

    @Autowired
    private WireMockServer wireMockServer;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    }

    @BeforeEach
    void init() {
        this.reviewRepository.deleteAll();
        this.bookRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        this.reviewRepository.deleteAll();
        this.bookRepository.deleteAll();
    }

    protected String getSignedJWT(String username, String email) throws JOSEException {
        return createJWT(username, email);
    }

    protected String getSignedJWT() throws JOSEException {
        return createJWT("duke", "duke@spring.io");
    }

    private String createJWT(String username, String email) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(RSAKeyGenerator.KEY_ID)
                .build();

        JWTClaimsSet payload = new JWTClaimsSet.Builder()
                .issuer(oAuth2Stubs.getIssuerUri())
                .audience("account")
                .subject(username)
                .claim("preferred_username", username)
                .claim("email", email)
                .claim("scope", "openid email profile")
                .claim("azp", "react-client")
                .claim("realm_access", Map.of("roles", List.of()))
                .expirationTime(Date.from(Instant.now().plusSeconds(120)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(header, payload);
        signedJWT.sign(new RSASSASigner(rsaKeyGenerator.getPrivateKey()));
        return signedJWT.serialize();
    }
}
