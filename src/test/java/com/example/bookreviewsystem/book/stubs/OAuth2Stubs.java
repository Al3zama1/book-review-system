package com.example.bookreviewsystem.book.stubs;

import com.example.bookreviewsystem.initializer.RSAKeyGenerator;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;


public class OAuth2Stubs {

    private final WireMockServer wireMockServer;
    private final RSAKeyGenerator rsaKeyGenerator;

    public OAuth2Stubs(WireMockServer wireMockServer, RSAKeyGenerator rsaKeyGenerator) {
        this.wireMockServer = wireMockServer;
        this.rsaKeyGenerator = rsaKeyGenerator;
    }

    public void stubForJWKS() {
        System.out.println(rsaKeyGenerator.getJWKSetJsonString());
        System.out.println("PUBLIC KEY STARTS HERE");
        System.out.println(rsaKeyGenerator.getPublicKey());
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

