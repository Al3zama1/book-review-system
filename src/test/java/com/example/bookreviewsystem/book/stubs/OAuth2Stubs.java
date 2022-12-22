package com.example.bookreviewsystem.book.stubs;

import com.example.bookreviewsystem.initializer.RSAKeyGenerator;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

/*
spring will try to access the configuration endpoint. OpenIdConfig file in resources
is an example of how it looks

as we can see in the file it went to the issuer uri and then it will pick up
the jwks_uri to get the certificate or public key in this case and will store it
inside the application to verify the signature
 */
public class OAuth2Stubs {

    private final WireMockServer wireMockServer;
    private final RSAKeyGenerator rsaKeyGenerator;

    public OAuth2Stubs(WireMockServer wireMockServer, RSAKeyGenerator rsaKeyGenerator) {
        this.wireMockServer = wireMockServer;
        this.rsaKeyGenerator = rsaKeyGenerator;
    }

    public void stubForJWKS() {
        System.out.println(rsaKeyGenerator.getJWKSetJsonString());
        wireMockServer.stubFor(
                WireMock.get("/jwks")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(rsaKeyGenerator.getJWKSetJsonString())
                        )
        );
    }

    public void stubForConfiguration() {
        wireMockServer.stubFor(
                //issuer uir + spring security will add .well-known/openid-configuration under the hood
                WireMock.get("/auth/realms/spring/.well-known/openid-configuration")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
            {
             "issuer":"%s",
             "jwks_uri":"%s"
            }
            """.formatted(getIssuerUri(), getJWKSUri())))
        );
    }

    // spring will check that the issuer is the same
    public String getIssuerUri() {
        return "http://localhost:" + wireMockServer.port() + "/auth/realms/spring";
    }

    private String getJWKSUri() {
        return "http://localhost:" + wireMockServer.port() + "/jwks";
    }
}

