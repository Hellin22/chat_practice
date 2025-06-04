package com.song.chatpractice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class ChatBackRabbitMqApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatBackRabbitMqApplication.class, args);
    }

}
