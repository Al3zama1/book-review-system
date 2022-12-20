package com.example.bookreviewsystem;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
public class BookReviewSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookReviewSystemApplication.class, args);
    }

    @SqsListener("default")
    public void listener(String message) {
        System.out.println(message);
    }
    @Bean
    CommandLineRunner commandLineRunner (QueueMessagingTemplate queueMessagingTemplate) {
        return runner -> {
            System.out.println("THE APPLICATION STARTED");
            queueMessagingTemplate.send("default", MessageBuilder.withPayload("dslfjsljflsdjf").build());
        };
    }

}
