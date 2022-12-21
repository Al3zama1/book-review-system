package com.example.bookreviewsystem.book.management;

public record BookDTO(String isbn, String title, String author, String description, String genre, Long pages, String publisher, String thumbnail) {
}
