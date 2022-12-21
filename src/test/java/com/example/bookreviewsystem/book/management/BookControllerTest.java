package com.example.bookreviewsystem.book.management;

import com.example.bookreviewsystem.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(WebSecurityConfig.class)
class BookControllerTest {

    @MockBean
    private BookService bookService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldGetEmptyArrayWhenNoBooksExist() throws Exception {
        mockMvc.perform(get("/api/books")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void shouldNotReturnXML() throws Exception {
        mockMvc.perform(get("/api/books")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void shouldReturnBooksWhenServiceReturnsBooks() throws Exception {
        // Given
        BookDTO bookOne = new BookDTO("42", "Java 14", "Mike", "Good Book",
                "Software Engineering", 200L, "Oracle", "ftp://localhost:42");
        BookDTO bookTwo = new BookDTO("42", "Java 15", "Duke", "Good Book",
                "Software Engineering", 200L, "Oracle", "ftp://localhost:42");

        given(bookService.getAllBooks()).willReturn(List.of(bookOne, bookTwo));

        // When
        MvcResult response = mockMvc.perform(get("/api/books")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].title", is("Java 14")))
                .andExpect(jsonPath("$[1].title", is("Java 15")))
                .andReturn();

        // Then
        String actualResponse = response.getResponse().getContentAsString();
        String expectResponse = objectMapper.writeValueAsString(List.of(bookOne, bookTwo));

        JSONAssert.assertEquals(expectResponse, actualResponse, true);
//        assertThat(actualResponse).isEqualTo(expectResponse);
    }


}