package com.example.bookreviewsystem.initializer;

import com.example.bookreviewsystem.book.stubs.OAuth2Stubs;
import com.example.bookreviewsystem.book.stubs.OpenLibraryStubs;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;

import java.util.Arrays;

@Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOG = LoggerFactory.getLogger(WireMockInitializer.class);


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        LOG.info("About to start the WireMockServer");

        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();

        LOG.info("WireMockServer successfully started");

        if (Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()).contains("integration-test")) {
            RSAKeyGenerator rsaKeyGenerator = new RSAKeyGenerator();
            rsaKeyGenerator.initializeKeys();

            OAuth2Stubs oAuth2Stubs = new OAuth2Stubs(wireMockServer, rsaKeyGenerator);

            /*
            we are initializing OAuth2 stubs because these have to be present before the application loads/starts
             */

            oAuth2Stubs.stubForJWKS();
            oAuth2Stubs.stubForConfiguration();

            applicationContext.getBeanFactory().registerSingleton("oAuth2Stubs", oAuth2Stubs);
            applicationContext.getBeanFactory().registerSingleton("rsaKeyGenerator", rsaKeyGenerator);

            /*
            TestPropertyValues isn't really designed with @SpringBootTest in mind. It's much more useful when you are
            writing tests that manually create an ApplicationContext. If you really want to use it with @SpringBootTest,
            it should be possible to via an ApplicationContextInitializer.
             */

            TestPropertyValues.of(
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:" +
                            wireMockServer.port() + "/auth/realms/spring"
                    ).applyTo(applicationContext);
        }

        /*
        OpenLibrary stubs do not need to be initialized since these will only be used during application runtime,
        they are not needed before the application loads/starts
         */

        OpenLibraryStubs openLibraryStubs = new OpenLibraryStubs(wireMockServer);

        applicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);
        applicationContext.getBeanFactory().registerSingleton("openLibraryStubs", openLibraryStubs);

        applicationContext.addApplicationListener(applicationEvent -> {
            /*
            This event gets thrown by spring when shutting down the context.
            We want to end the wireMockServer at that point too
             */
            if (applicationEvent instanceof ContextClosedEvent) {
                LOG.info("Stopping the WireMockServer");
                wireMockServer.stop();
            }
        });

        /*
        OpenLibrary points to the mocked server for making integration tests. We don't want to make actual calls
        to the real system out to the internet because the system we are making calls to could be temporarily down.
        Making our tests fail, even though our implementation is correct.
        For integration tests in general I don't think we are supposed to make real calls out ot the
        internet, instead we mock a server like wiremock
         */

        TestPropertyValues.of(
                "clients.open-library.base-url=http://localhost:" + wireMockServer.port() + "/openLibrary"
                ).applyTo(applicationContext);

    }
}
