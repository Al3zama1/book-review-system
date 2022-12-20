package com.example.bookreviewsystem.book;

import com.jayway.jsonpath.JsonPath;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JsonTest {
    /*
    JSONAssert does not provide that much flexibility for testing individual parts of the
    JSON string. It is stricter, therefore it is better for testing the whole JSON
    document/string
     */

    @Test
    void testWithJSONAssert() throws JSONException {
        // Give
        String result = """
      {
        "name": "duke",
        "age": "42",
        "hobbies": [
          "soccer",
          "java"
        ]
      }
      """;

        // strict = false -> ignore all other json values, mark it as true if given json property and its values are the same
        JSONAssert.assertEquals("{\"name\": \"duke\"}", result, false);
        JSONAssert.assertNotEquals("{\"name\": \"duke\"}", result, true);
        // order of property values does not matter, but they all need to be there
        JSONAssert.assertEquals("{\"hobbies\": [\"soccer\", \"java\"]}", result, false);
    }

    /*
    JsonPath is good for making assertions for parts of the json String since it provides more flexibility
    to select individual parts of the JSON string
     */
    @Test
    void testWithJsonPath() {
        String result = """
      {
        "name": "duke",
        "age": "42",
        "tags": [
          "jdk",
          "java"
        ],
        "orders": [42, 42, 16]
      }
      """;

        assertThat(JsonPath.parse(result).read("$.tags.length()", Long.class)).isEqualTo(2);
        assertThat(JsonPath.parse(result).read("$.name", String.class)).isEqualTo("duke");
        assertThat(JsonPath.parse(result).read("$.orders.sum()", Integer.class)).isEqualTo(100);
    }
}
