package com.example.bookreviewsystem.book.management.client;

import com.example.bookreviewsystem.book.management.BookEntity;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OpenLibraryApiClientTest {
    private MockWebServer mockWebServer;
    private OpenLibraryApiClient cut;

    private static final String ISBN = "9780596004651";
    private static String VALID_RESPONSE;

    static {
        try {
            VALID_RESPONSE = new String(OpenLibraryApiClientTest.class
                    .getClassLoader()
                    .getResourceAsStream("stubs/openlibrary/success-" + ISBN + ".json")
                    .readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    having a new web server for each test method, we ensure that there is not any
    enqueued responses from a previous test to avoid possible side effects that could
    make our test fail
     */

    @BeforeEach
    public void setup() throws IOException {
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1_000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(1))
                        .addHandlerLast(new WriteTimeoutHandler(1)));

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        cut = new OpenLibraryApiClient(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(mockWebServer.url("/").toString())
                .build());
    }

    @Test
    void shouldReturnBookWhenResultIsSuccess() throws InterruptedException {
        // Given
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; character=urf-8")
                .setResponseCode(200)
                .setBody(VALID_RESPONSE);

        mockWebServer.enqueue(mockResponse);

        // When
        BookEntity result = cut.fetchMetadataForBook(ISBN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo(ISBN);
        assertThat(result.getTitle()).isEqualTo("Head first Java");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://covers.openlibrary.org/b/id/388761-S.jpg");
        assertThat(result.getAuthor()).isEqualTo("Kathy Sierra");
        assertThat(result.getDescription()).isEqualTo("Your brain on Java--a learner's guide--Cover.Includes index.");
        assertThat(result.getGenre()).isEqualTo("Java (Computer program language)");
        assertThat(result.getPublisher()).isEqualTo("O'Reilly");
        assertThat(result.getPages()).isEqualTo(619);
        // we don't want an Id yet since it should be created by our DB later
        assertThat(result.getId()).isNull();

        // RecordedRequest can be used to verify thinkgs like headers, uri, body, etc...
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo(
                "/api/books?jscmd=data&format=json&bibkeys=ISBN:9780596004651");
    }

    @Test
    void shouldReturnBookWhenResultIsSuccessButLackingAllInformation() {
        // Given
        String response = """
      {
        "ISBN:9780596004651": {
          "publishers": [
            {
              "name": "O'Reilly"
            }
          ],
          "title": "Head second Java",
          "authors": [
            {
            "url": "https://openlibrary.org/authors/OL1400543A/Kathy_Sierra",
            "name": "Kathy Sierra"
            }
          ],
          "number_of_pages": 42,
          "cover": {
            "small": "https://covers.openlibrary.org/b/id/388761-S.jpg",
            "large": "https://covers.openlibrary.org/b/id/388761-L.jpg",
            "medium": "https://covers.openlibrary.org/b/id/388761-M.jpg"
          }
        }
      }
      """;

        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(200)
                .setBody(response);

        mockWebServer.enqueue(mockResponse);

        // When
        BookEntity result = cut.fetchMetadataForBook(ISBN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo(ISBN);
        assertThat(result.getTitle()).isEqualTo("Head second Java");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://covers.openlibrary.org/b/id/388761-S.jpg");
        assertThat(result.getAuthor()).isEqualTo("Kathy Sierra");
        assertThat(result.getDescription()).isEqualTo("n.A");
        assertThat(result.getGenre()).isEqualTo("n.A");
        assertThat(result.getPublisher()).isEqualTo("O'Reilly");
        assertThat(result.getPages()).isEqualTo(42);
        // we don't want an Id yet since it should be created by our DB later
        assertThat(result.getId()).isNull();
    }

    @Test
    void shouldFailWhenRemoteSystemIsDown() {
        // Given
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .setBody("Sorry, system is down :(");

        mockWebServer.enqueue(mockResponse);

        // When, Then
        assertThatThrownBy(() -> cut.fetchMetadataForBook(ISBN))
                .isInstanceOf(RuntimeException.class);
    }

    /*
    make the client more resilient
     */
    @Test
    void shouldRetryWhenRemoteSystemIsSlowOrFailing() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Sorry, system is down :("));

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(200)
                .setBody(VALID_RESPONSE)
                .setBodyDelay(2, TimeUnit.SECONDS));

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(200)
                .setBody(VALID_RESPONSE));

        // When
        BookEntity result = cut.fetchMetadataForBook(ISBN);

        // Then
        assertThat(result.getIsbn()).isEqualTo("9780596004651");
        assertThat(result.getId()).isNull();


    }
}